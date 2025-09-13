package com.bin.repository;

import com.bin.dto.SocializeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocializeDocumentRepository extends ElasticsearchRepository<SocializeDocument, Long> {

    // 根据内容搜索评论
    Page<SocializeDocument> findByContentContaining(String keyword, Pageable pageable);

    // 根据用户ID搜索评论
    Page<SocializeDocument> findByUserId(Long userId, Pageable pageable);

    // 根据目标ID搜索评论
    Page<SocializeDocument> findByTargetId(Long targetId, Pageable pageable);

}
