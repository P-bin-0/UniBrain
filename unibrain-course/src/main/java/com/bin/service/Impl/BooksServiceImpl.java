package com.bin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Books;
import com.bin.dto.vo.BooksPageQuery;
import com.bin.dto.vo.BooksVo;
import com.bin.dto.vo.PageResult;
import com.bin.mapper.BooksMapper;
import com.bin.service.BooksService;
import com.bin.utils.RedisCacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课程相关书籍实现类
 * @author bin
 */
@Service
public class BooksServiceImpl extends ServiceImpl<BooksMapper, Books> implements BooksService {
    @Autowired
    private BooksMapper booksMapper;
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页查询书籍
     * @param booksPageQuery 书籍分页查询参数
     * @return 书籍VO列表
     */
    @Override
    public PageResult<BooksVo> pageQuery(BooksPageQuery booksPageQuery) {
        String cacheKey = generateCacheKey(booksPageQuery);
        String lockKey = cacheKey + ":lock";

        // 1. 尝试从缓存读取
        String cachedJson = null;
        try {
            cachedJson = redisCacheUtil.getCache(cacheKey);
        } catch (Exception e) {
            log.warn("从Redis获取缓存失败，将查询数据库");
        }

        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                return objectMapper.readValue(cachedJson, new TypeReference<PageResult<BooksVo>>() {
                });
            } catch (Exception e) {
                log.warn("缓存数据反序列化失败，将查询数据库");
            }
        }

        // 2. 缓存未命中，尝试加锁重建（防击穿）
        try {
            Boolean isLocked = redisCacheUtil.tryLock(lockKey, 3, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isLocked)) {
                // 执行数据库查询
                PageResult<BooksVo> dbResult = getBooksVoPageResult(booksPageQuery);

                // 序列化并写入缓存
                try {
                    String json = objectMapper.writeValueAsString(dbResult);
                    // 防雪崩：添加随机过期时间
                    long baseExpire = dbResult.getTotal() > 0 ? 10 : 2; // 有数据10分钟，空数据2分钟
                    long randomExpire = baseExpire + ThreadLocalRandom.current().nextInt(3); // 随机 +0~2分钟
                    redisCacheUtil.setCache(cacheKey, json, randomExpire, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("将数据存入Redis缓存失败", e);
                }
                return dbResult;
            } else {
                // 获取锁失败，说明其他线程正在重建缓存
                // 短暂等待后重试一次（避免雪崩期间大量请求穿透）
                Thread.sleep(50);
                return pageQuery(booksPageQuery);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("缓存重建被中断，直接查询数据库");
            return getBooksVoPageResult(booksPageQuery);
        } finally {
            // 释放锁
            redisCacheUtil.unlock(lockKey);
        }
    }

    /**
     * 添加书籍
     * @param books 书籍信息
     */
    @Override
    public void addBook(Books books) {
        int count = booksMapper.insert(books);
        if (count > 0) {
            //查询新添加的书籍
            Books newBook = booksMapper.selectByName(books.getName());
            //添加缓存
            redisCacheUtil.setObject("book:" + newBook.getId(), newBook, 10, TimeUnit.MINUTES);
            //删除所有相关的缓存（分页查询缓存）
            redisCacheUtil.deleteByPattern("books:page:*");
        }else {
            log.error("添加书籍失败");
            throw new RuntimeException("添加书籍失败");
        }

    }

    /**
     * 删除书籍(批量或单都可)
     * @param ids 书籍id列表
     * @return 删除成功的书籍数量
     */
    @Override
    public int deleteByIds(List<Long> ids) {
        //检验ids是否存在无效数据
        List<Books> books = booksMapper.selectByIds(ids);
        if (books.size() != ids.size()) {
            log.error("删除书籍失败，存在无效数据");
            throw new RuntimeException("删除书籍失败，存在无效数据");
        }
        //删除数据库数据
        int count = booksMapper.deleteByIds(ids);
        //删除缓存
        //批量删除单条数据缓存（books:1001..）
        List<String> singleKeys = ids.stream()
                .map(id -> "books:" + id)
                .toList();
        redisCacheUtil.deleteBatch(singleKeys);
        //删除相关集合缓存（books:page:1）
        redisCacheUtil.deleteByPattern("books:page:*");
        return count;
    }

    /**
     * 更新书籍
     * @param books 书籍信息
     */
    @Override
    public void updateBook(Books books) {
        Books existBook = booksMapper.selectById(books.getId());
        if (existBook == null) {
            log.error("更新书籍失败，书籍不存在");
            throw new RuntimeException("更新书籍失败，书籍不存在");
        }
        //更新数据
        booksMapper.updateById(books);
        //删除单条数据缓存
        String cacheKey = "books:" + books.getId();
        redisCacheUtil.delete(cacheKey);
        //删除相关集合缓存（books:page:1）
        redisCacheUtil.deleteByPattern("books:page:*");
    }

    /**
     * 根据id查询书籍
     * @param id 书籍id
     * @return 书籍信息
     */
    @Override
    public Books getBookById(Long id) {
        String cacheKey = "books:" + id;
        //1.尝试从缓存获取
        Books books = redisCacheUtil.getObject(cacheKey, Books.class);
        if (books != null) {
            return books;
        }
        //2.缓存未命中，查询数据库
        books = booksMapper.selectById(id);
        if (books != null) {
            //3.将查询结果放入缓存
            redisCacheUtil.setObject(cacheKey, books, 10, TimeUnit.MINUTES);
        }
        return books;
    }

    /**
     * 分页查询书籍VO
     * @param booksPageQuery 书籍分页查询参数
     * @return 书籍VO列表
     */
    @NotNull
    private PageResult<BooksVo> getBooksVoPageResult(BooksPageQuery booksPageQuery) {
        //1.创建分页查询对象
        Page<Books> page = new Page<>(booksPageQuery.getPageNum(), booksPageQuery.getPageSize());
        //2.构建查询条件
        LambdaQueryWrapper<Books> wrapper = new LambdaQueryWrapper<>();
        if (booksPageQuery.getName() != null) {
            wrapper.like(Books::getName, booksPageQuery.getName());
        }
        if (booksPageQuery.getPublisher() != null) {
            wrapper.like(Books::getPublisher, booksPageQuery.getPublisher());
        }
        if (booksPageQuery.getPublicationYear() != null) {
            wrapper.eq(Books::getPublicationYear, booksPageQuery.getPublicationYear());
        }
        if (booksPageQuery.getEdition() != null) {
            wrapper.like(Books::getEdition, booksPageQuery.getEdition());
        }
        if (booksPageQuery.getSubject() != null) {
            wrapper.like(Books::getSubject, booksPageQuery.getSubject());
        }
        if (booksPageQuery.getCourseName() != null) {
            wrapper.like(Books::getCourseName, booksPageQuery.getCourseName());
        }
        if (booksPageQuery.getCreateAt() != null) {
            wrapper.ge(Books::getCreateAt, booksPageQuery.getCreateAt());
        }
        if (booksPageQuery.getUpdateAt() != null) {
            wrapper.ge(Books::getUpdateAt, booksPageQuery.getUpdateAt());
        }
        wrapper.orderByDesc(Books::getCreateAt);
        //3.执行查询
        Page<Books> booksPage = booksMapper.selectPage(page,wrapper);
        //4.转换为VO对象
        List<BooksVo> booksVoList = booksPage.getRecords().stream()
                .map(book -> {
                    BooksVo booksVo = new BooksVo();
                    BeanUtil.copyProperties(book, booksVo);
                    return booksVo;
                }).toList();
        //5.构建分页结果返回
        return new PageResult<>(
                booksPage.getTotal(),
                booksPage.getCurrent(),
                booksPage.getSize(),
                booksVoList
        );
    }

    /**
     * 生成缓存key
     * @param booksPageQuery 书籍分页查询参数
     * @return 缓存key
     */
    private String generateCacheKey(BooksPageQuery booksPageQuery) {
        StringBuilder key = new StringBuilder("books:page:");
        key.append(booksPageQuery.getPageNum())
                .append("_").append(booksPageQuery.getPageSize());

        appendParam(key, "name", booksPageQuery.getName());
        appendParam(key, "publisher", booksPageQuery.getPublisher());
        appendParam(key, "year", booksPageQuery.getPublicationYear());
        appendParam(key, "edition", booksPageQuery.getEdition());
        appendParam(key, "subject", booksPageQuery.getSubject());
        appendParam(key, "course", booksPageQuery.getCourseName());

        return key.toString();
    }

    private void appendParam(StringBuilder key, String paramName, Object value) {
        if (value != null) {
            key.append("_").append(paramName).append("-").append(value);
        }
    }
}
