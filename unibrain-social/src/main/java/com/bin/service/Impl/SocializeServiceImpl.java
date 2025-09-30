package com.bin.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.*;
import com.bin.dto.vo.SocializeVO;
import com.bin.mapper.SocializeMapper;
import com.bin.mapper.UserMapper;
import com.bin.model.entity.User;
import com.bin.repository.SocializeDocumentRepository;
import com.bin.service.SocializeService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


/**
 * 社交服务实现类
 */
@Service
public class SocializeServiceImpl extends ServiceImpl<SocializeMapper, Socialize> implements SocializeService {

    private static final Logger logger = LoggerFactory.getLogger(SocializeServiceImpl.class);
    @Autowired
    private SocializeMapper socializeMapper;

    @Autowired
    private SocializeDocumentRepository socializeDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ElasticsearchClient client;

    // 滚动上下文有效期
    private static final String SCROLL_TIME = "1m"; // 1分钟


    /**
     * 发表评论
     */
    @Override
    public void insertComment(SocializeDTO socializeDTO) {
        // 先判断评论是否为空
        if (socializeDTO.getContent() == null) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        // 转换为实体类
        Socialize socialize = new Socialize();
        BeanUtils.copyProperties(socializeDTO, socialize);
        // 将点赞数，评论数，转发数都设置为0
        socialize.setLikeCount(0L);
        socialize.setContentCount(0L);
        socialize.setForwardCount(0L);
        // 设置插入时间
        socialize.setCreateAt(LocalDateTime.now());
        // 插入数据库
        socializeMapper.insert(socialize);
        // 同步到Elasticsearch
        try {
            SocializeDocument document = convertToDocument(socialize);
            socializeDocumentRepository.save(document);
            logger.info("评论数据成功同步到Elasticsearch, ID: {}", socialize.getId());
        } catch (Exception e) {
            logger.error("同步评论数据到Elasticsearch失败, ID: {}, 错误: {}", socialize.getId(), e.getMessage());
            // 可以选择记录到重试队列或忽略，不影响主业务流程
        }
    }

    /**
     * 删除评论
     */
    @Override
    public void removeCommentById(Long id) {
        // 判断ID是否为空
        if (id == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        // 判断是否存在该评论
        Socialize socialize = socializeMapper.selectById(id);
        if (socialize == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        // 删除评论
        socializeMapper.deleteById(id);
        // 删除子评论
        socializeMapper.delete(
                new LambdaQueryWrapper<Socialize>()
                        .eq(Socialize::getParentId, id));
        // 同步删除ES中的数据
        try {
            socializeDocumentRepository.deleteById(id);
            logger.info("评论数据成功从Elasticsearch删除, ID: {}", id);
        } catch (Exception e) {
            logger.error("从Elasticsearch删除评论数据失败, ID: {}, 错误: {}", id, e.getMessage());
        }
    }

    /**
     * 搜索评论
     */
    /*@Override
    public List<SocializeVO> searchComment(String keyword, int page, int size) {
        // 分页查询
        Page<Socialize> pageInfo = new Page<>(page, size);
        // 构建查询条件
        LambdaQueryWrapper<Socialize> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Socialize::getContent, keyword);
        // 执行查询
        Page<Socialize> socializePage = socializeMapper.selectPage(pageInfo, queryWrapper);
        // 转换为视图对象
        List<SocializeVO> socializeVOList = socializePage.getRecords().stream()
                .map(socialize -> {
                    SocializeVO socializeVO = new SocializeVO();
                    BeanUtils.copyProperties(socialize, socializeVO);
                    return socializeVO;
                })
                .toList();
        // 返回结果
        return socializeVOList;
    }*/
    @Override
    public PageResult<SocializeVO> searchComment(String keyword, PageQueryDTO pageQueryDTO) {
        int maxPage = 1000; // 可配置在配置文件中
        if (pageQueryDTO.getPage() > maxPage) {
            throw new IllegalArgumentException("页码超过上限，请使用滚动加载");
        }
        // 使用Elasticsearch进行搜索
        PageRequest pageRequest = PageRequest.of(
                pageQueryDTO.getPage() - 1,
                pageQueryDTO.getPageSize(),
                Sort.by(Sort.Direction.fromString(pageQueryDTO.getSortDir()), pageQueryDTO.getSortField()));
        Page<SocializeDocument> documentPage;
        try {
            documentPage = socializeDocumentRepository.findByContentContaining(keyword, pageRequest);
        } catch (Exception e) {
            logger.error("ES搜索评论失败，关键词：{}，分页参数：{}", keyword, pageQueryDTO, e);
            throw new RuntimeException("搜索失败，请稍后重试");
        }

        // 转换为VO对象
        List<SocializeVO> socializeVOList = documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .toList();
        // 返回结果
        return new PageResult<>(socializeVOList, documentPage.getTotalElements(), documentPage.getNumber() + 1, documentPage.getSize());
    }
    // 按用户ID搜索评论
    @Override
    public SocializeVO searchByCommentIdScrollInit(Long id) {
        // 分页查询
        /*PageRequest pageRequest = PageRequest.of(
                pageQueryDTO.getPage() - 1,
                pageQueryDTO.getPageSize(),
                Sort.by(Sort.Direction.fromString(pageQueryDTO.getSortDir()), pageQueryDTO.getSortField()));*/
        SocializeDocument document = socializeDocumentRepository.findById(id).orElse(null);
        // 转换为VO对象
        SocializeVO vo = new SocializeVO();
        if (document != null) {
            BeanUtils.copyProperties(document, vo);
        }
        // 返回结果
        return vo;
    }

    // 按目标ID搜索（如某篇文章的所有评论）
    @Override
    public PageResult<SocializeVO> searchByTargetId(Long targetId, PageQueryDTO pageQueryDTO) {
        // 分页查询
        PageRequest pageRequest = PageRequest.of(
                pageQueryDTO.getPage() - 1,
                pageQueryDTO.getPageSize(),
                Sort.by(Sort.Direction.fromString(pageQueryDTO.getSortDir()), pageQueryDTO.getSortField()));
        Page<SocializeDocument> documentPage = socializeDocumentRepository.findByTargetId(targetId, pageRequest);
        // 转换为VO对象
        List<SocializeVO> socializeVOList = documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .toList();
        // 返回结果
        return new PageResult<>(socializeVOList, documentPage.getTotalElements(), documentPage.getNumber() + 1, documentPage.getSize());
    }

    /**
     * 查询所有评论
     */
    @Override
    public PageResult<SocializeVO> selectAll(PageQueryDTO pageQueryDTO) {
        // 分页查询所有评论
        PageRequest pageRequest = PageRequest.of(pageQueryDTO.getPage() - 1, pageQueryDTO.getPageSize(), Sort.by(Sort.Direction.DESC, "createAt"));
        Page<SocializeDocument> documentPage = socializeDocumentRepository.findAll(pageRequest);
        // 转换为VO对象
        List<SocializeVO> socializeVOList = documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .toList();
        // 返回结果
        return new PageResult<>(socializeVOList, documentPage.getTotalElements(), documentPage.getNumber() + 1, documentPage.getSize());
    }

    // 添加一个方法用于初始化ES数据（用于将现有MySQL数据导入ES）
    @PostConstruct
    public void initElasticsearchData() {
        // 异步执行初始化
        CompletableFuture.runAsync(() -> {
            try {
                // 检查索引是否存在
                if (!elasticsearchOperations.indexOps(SocializeDocument.class).exists()) {
                    // 创建索引
                    elasticsearchOperations.indexOps(SocializeDocument.class).create();
                    logger.info("Elasticsearch索引创建成功");

                    // 映射设置
                    elasticsearchOperations.indexOps(SocializeDocument.class).putMapping();
                    logger.info("Elasticsearch映射设置成功");
                }

                // 检查ES中是否有数据
                long esCount = socializeDocumentRepository.count();
                if (esCount == 0) {
                    logger.info("开始从MySQL导入数据到Elasticsearch");

                    // 将MySQL中的所有数据导入ES
                    List<Socialize> allSocializes = socializeMapper.selectList(null);
                    if (allSocializes.isEmpty()) {
                        logger.info("MySQL中无评论数据，无需导入");
                        return;
                    }
                    // 批量查询所有需要的userId对应的用户名（避免N+1问题）
                    Set<Long> userIds = allSocializes.stream()
                            .map(Socialize::getUserId)
                            .collect(Collectors.toSet());
                    // 批量查询user表(1次查询）
                    List<User> users = userMapper.selectByIds(userIds);
                    // 构建用户id到用户名的映射
                    Map<Long,String> userIdToNameMap = users.stream()
                            .collect(Collectors.toMap(
                                    User::getId,
                                    User::getName,
                                    (k1,k2) -> k1
                            ));
                    List<SocializeDocument> documents = allSocializes.stream()
                            .map(socialize -> {
                                // 从批量映射中获取用户名，确保不会为null
                                String userName = userIdToNameMap.getOrDefault(socialize.getUserId(), "未知用户");
                                // 直接构建文档，不调用convertToDocument（避免重复查询）
                                return SocializeDocument.builder()
                                        .id(socialize.getId())
                                        .userId(socialize.getUserId())
                                        .userName(userName)  // 使用批量映射的用户名
                                        .content(socialize.getContent())
                                        .createAt(socialize.getCreateAt())
                                        .likeCount(socialize.getLikeCount())
                                        .contentCount(socialize.getContentCount())
                                        .forwardCount(socialize.getForwardCount())
                                        .targetId(socialize.getTargetId())
                                        .parentId(socialize.getParentId())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    if (!documents.isEmpty()) {
                        socializeDocumentRepository.saveAll(documents);
                        logger.info("成功导入{}条数据到Elasticsearch", documents.size());
                    }
                } else {
                    logger.info("Elasticsearch中已有{}条数据，跳过初始化", esCount);
                }
            } catch (Exception e) {
                logger.error("初始化Elasticsearch数据失败: {}", e.getMessage(), e);
            }
        }, executorService); // 需要注入一个线程池
    }
    private SocializeDocument convertToDocument(Socialize socialize) {
        User user = userMapper.selectById(socialize.getUserId());
        String username = user != null ? user.getName() : "未知用户";
        return SocializeDocument.builder()
                .id(socialize.getId())
                .userId(socialize.getUserId())
                .userName(username)
                .content(socialize.getContent())
                .createAt(socialize.getCreateAt())
                .likeCount(socialize.getLikeCount())
                .contentCount(socialize.getContentCount())
                .forwardCount(socialize.getForwardCount())
                .targetId(socialize.getTargetId())
                .parentId(socialize.getParentId())
                .build();
    }

}