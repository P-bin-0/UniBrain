package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Socialize;
import com.bin.dto.SocializeDTO;
import com.bin.dto.SocializeDocument;
import com.bin.dto.vo.SocializeVO;
import com.bin.mapper.SocializeMapper;
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

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * 发表评论
     */
    @Override
    public void insertComment(SocializeDTO socializeDTO) {
        // 先判断评论是否为空
        if (socializeDTO.getContent() == null) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        // 将点赞数，评论数，转发数都设置为0
        socializeDTO.setLikeCount(0L);
        socializeDTO.setContentCount(0L);
        socializeDTO.setForwardCount(0L);
        // 转换为实体类
        Socialize socialize = new Socialize();
        BeanUtils.copyProperties(socializeDTO, socialize);
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
    public List<SocializeVO> searchComment(String keyword, int page, int size) {
        // 使用Elasticsearch进行搜索
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<SocializeDocument> documentPage = socializeDocumentRepository.findByContentContaining(keyword, pageRequest);

        // 转换为VO对象
        return documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
    // 按用户ID搜索
    public List<SocializeVO> searchByUserId(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<SocializeDocument> documentPage = socializeDocumentRepository.findByUserId(userId, pageRequest);
        return documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // 按目标ID搜索（如某篇文章的所有评论）
    public List<SocializeVO> searchByTargetId(Long targetId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<SocializeDocument> documentPage = socializeDocumentRepository.findByTargetId(targetId, pageRequest);
        return documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .collect(Collectors.toList());
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
                    List<SocializeDocument> documents = allSocializes.stream()
                            .map(this::convertToDocument)
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
        return SocializeDocument.builder()
                .id(socialize.getId())
                .userId(socialize.getUserId())
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