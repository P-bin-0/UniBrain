package com.bin.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.*;
import com.bin.dto.vo.SocializeVO;
import com.bin.entity.User;
import com.bin.mapper.SocializeMapper;
import com.bin.mapper.UserMapper;
import com.bin.repository.SocializeDocumentRepository;
import com.bin.service.SocializeService;
import com.bin.util.SecurityUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
        // 查询该评论是否是当前登录用户的评论
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!socialize.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("您没有权限删除该评论");
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
        // 使用Elasticsearch进行搜索
        PageRequest pageRequest = PageRequest.of(
                pageQueryDTO.getPage() - 1,
                pageQueryDTO.getPageSize(),
                Sort.by(Sort.Direction.fromString(pageQueryDTO.getSortDir()), pageQueryDTO.getSortField()));
        Page<SocializeDocument> documentPage;
        try {
            documentPage = socializeDocumentRepository.findByParentIdAndContentContaining(0L, keyword, pageRequest);
        } catch (Exception e) {
            logger.error("ES搜索评论失败，关键词：{}，分页参数：{}", keyword, pageQueryDTO, e);
            throw new RuntimeException("搜索失败，请稍后重试");
        }

        // 2. 转换顶级评论为VO
        List<SocializeVO> topVoList = documentPage.getContent().stream()
                .map(doc -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(doc, vo);
                    vo.setParentId(Objects.requireNonNullElse(doc.getParentId(), 0L));
                    return vo;
                })
                .collect(Collectors.toList());

        // 3. 批量查询这些顶级评论下的子评论（且子评论内容也包含关键词）
        if (!topVoList.isEmpty()) {
            List<Long> topIds = topVoList.stream().map(SocializeVO::getId).collect(Collectors.toList());
            for (Long topId : topIds) {
                // 搜索子评论（parentId=顶级ID 且 内容含关键词）
                List<SocializeDocument> childDocs = socializeDocumentRepository
                        .findByParentIdAndContentContaining(topId, keyword, Sort.by("createAt"));
                // 转换并挂载子评论（递归处理层级）
                List<SocializeVO> childVos = childDocs.stream()
                        .map(doc -> {
                            SocializeVO vo = new SocializeVO();
                            BeanUtils.copyProperties(doc, vo);
                            vo.setParentId(Objects.requireNonNullElse(doc.getParentId(), topId));
                            vo.setChildren(getChildrenByParentIdAndKeyword(vo.getId(), keyword)); // 递归查孙子评论
                            return vo;
                        })
                        .collect(Collectors.toList());
                // 挂载到对应的顶级评论
                topVoList.stream()
                        .filter(vo -> vo.getId().equals(topId))
                        .findFirst()
                        .ifPresent(vo -> vo.setChildren(childVos));
            }
        }
        // 返回结果
        return new PageResult<>(topVoList, documentPage.getTotalElements(), documentPage.getNumber() + 1, documentPage.getSize());
    }
    /**
     * 按评论ID查询（含完整层级：当前评论+所有子评论）
     * @param id 评论ID
     * @return 带完整层级的评论VO
     */
    @Override
    public SocializeVO searchByCommentIdScrollInit(Long id) {
        // 1. 先查询当前评论的基础信息
        Optional<SocializeDocument> docOpt = socializeDocumentRepository.findById(id);
        if (docOpt.isEmpty()) {
            logger.warn("评论ID:{}不存在", id);
            return null;
        }
        // 2. 转换为VO
        SocializeVO currentVO = new SocializeVO();
        BeanUtils.copyProperties(docOpt.get(), currentVO);

        // 3. 递归查询所有子评论（按parentId=当前评论ID）
        List<SocializeVO> childrenVO = getChildrenByParentId(id);
        currentVO.setChildren(childrenVO);

        return currentVO;
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
     * 查询所有评论（分页+层级组装）
     * @param pageQueryDTO 分页参数
     * @return 分页的带层级评论列表
     */
    @Override
    public PageResult<SocializeVO> selectAll(PageQueryDTO pageQueryDTO) {
        // 分页查询所有评论
        PageRequest pageRequest = PageRequest.of(pageQueryDTO.getPage() - 1, pageQueryDTO.getPageSize(), Sort.by(Sort.Direction.DESC, "createAt"));
        Page<SocializeDocument> documentPage = socializeDocumentRepository.findByParentId(0L, pageRequest);
        // 转换为VO对象
        List<SocializeVO> socializeVOList = documentPage.getContent().stream()
                .map(document -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(document, vo);
                    return vo;
                })
                .toList();
        // 3. 批量查询所有顶级评论的子评论（避免N+1查询）
        if (!socializeVOList.isEmpty()) {
            // 3.1 收集所有顶级评论ID
            List<Long> topCommentIds = socializeVOList.stream()
                    .map(SocializeVO::getId)
                    .toList();

            // 3.2 查询这些顶级评论的所有子评论（一次查询，parentId in 顶级评论ID）
            List<SocializeDocument> allChildDocs = new ArrayList<>();
            for (Long topId : topCommentIds) {
                // 调用已有方法查询子评论（按创建时间正序）
                List<SocializeDocument> childDocs = socializeDocumentRepository.findByParentId(topId, Sort.by(Sort.Direction.ASC, "createAt"));
                allChildDocs.addAll(childDocs);
            }

            // 3.3 转换子评论为VO，并按parentId分组（方便挂载）
            Map<Long, List<SocializeVO>> childVoGroupByParentId = allChildDocs.stream()
                    .map(doc -> {
                        SocializeVO vo = new SocializeVO();
                        BeanUtils.copyProperties(doc, vo);
                        return vo;
                    })
                    .collect(Collectors.groupingBy(SocializeVO::getParentId));

            // 3.4 给每个顶级评论挂载子评论（并递归加载子评论的子评论）
            for (SocializeVO topVo : socializeVOList) {
                List<SocializeVO> directChildren = childVoGroupByParentId.getOrDefault(topVo.getId(), Collections.emptyList());
                // 递归加载子评论的子评论（复用已有方法）
                List<SocializeVO> childrenWithHierarchy = directChildren.stream()
                        .map(childVo -> {
                            childVo.setChildren(getChildrenByParentId(childVo.getId()));
                            return childVo;
                        })
                        .collect(Collectors.toList());
                topVo.setChildren(childrenWithHierarchy);
            }
        }
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
                                    User::getUsername,
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
        String username = user != null ? user.getUsername() : "未知用户";
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

    /**
     * 辅助方法：按父评论ID查询所有子评论（递归）
     * @param parentId 父评论ID
     * @return 子评论VO列表（带层级）
     */
    private List<SocializeVO> getChildrenByParentId(Long parentId) {
        // 1. 从ES查询该父评论下的所有子评论（按创建时间正序，旧回复在前）
        Sort sort = Sort.by(Sort.Direction.ASC, "createAt");
        List<SocializeDocument> childDocs = socializeDocumentRepository.findByParentId(parentId, sort);

        // 2. 转换为VO并递归查询子评论的子评论
        return childDocs.stream()
                .map(doc -> {
                    SocializeVO childVO = new SocializeVO();
                    BeanUtils.copyProperties(doc, childVO);
                    // 递归查询当前子评论的子评论
                    childVO.setChildren(getChildrenByParentId(doc.getId()));
                    return childVO;
                })
                .collect(Collectors.toList());
    }
    // 辅助方法：递归查询子评论（含关键词）
    private List<SocializeVO> getChildrenByParentIdAndKeyword(Long parentId, String keyword) {
        List<SocializeDocument> childDocs = socializeDocumentRepository
                .findByParentIdAndContentContaining(parentId, keyword, Sort.by("createAt"));
        return childDocs.stream()
                .map(doc -> {
                    SocializeVO vo = new SocializeVO();
                    BeanUtils.copyProperties(doc, vo);
                    vo.setParentId(Objects.requireNonNullElse(doc.getParentId(), parentId));
                    vo.setChildren(getChildrenByParentIdAndKeyword(doc.getId(), keyword));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}