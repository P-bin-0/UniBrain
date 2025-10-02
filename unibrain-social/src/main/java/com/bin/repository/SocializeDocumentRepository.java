package com.bin.repository;

import com.bin.dto.SocializeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocializeDocumentRepository extends ElasticsearchRepository<SocializeDocument, Long> {

    // 根据内容搜索评论
    Page<SocializeDocument> findByContentContaining(String keyword, Pageable pageable);

    // 仅搜索顶级评论（parentId=0）且内容包含关键词
    Page<SocializeDocument> findByParentIdAndContentContaining(Long parentId, String keyword, Pageable pageable);

    // 2. 用于查询子评论（返回List，仅需排序，用Sort参数）
    // 调用场景：给顶级评论加载子评论时，不需要分页但需要排序
    List<SocializeDocument> findByParentIdAndContentContaining(Long parentId, String keyword, Sort sort);

    /**
     * 按父评论ID查询子评论（支持排序）
     * @param parentId 父评论ID
     * @param sort 排序规则
     * @return 子评论列表
     */
    List<SocializeDocument> findByParentId(Long parentId, Sort sort);

    /**
     * 新增：按parentId分页查询（用于查询顶级评论，parentId=0）
     */
    Page<SocializeDocument> findByParentId(Long parentId, Pageable pageable);

    // 根据目标ID搜索评论
    Page<SocializeDocument> findByTargetId(Long targetId, Pageable pageable);

    /**
     * 查询所有评论（分页）
     * @param pageable 分页参数
     * @return 分页的扁平评论列表
     */
    Page<SocializeDocument> findAll(Pageable pageable);
}
