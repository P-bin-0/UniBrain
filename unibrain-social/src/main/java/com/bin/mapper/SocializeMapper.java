package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Socialize;
import com.bin.dto.vo.SocializeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SocializeMapper extends BaseMapper<Socialize> {
    /**
     * 查询所有评论
     */
    @Select("""
            select 
                s.id,
                u.name as userName,
                s.content,
                s.create_at,
                s.like_count,
                s.content_count,
                s.forward_count,
                s.target_id,
                s.parent_id
            from socialize s
            left join user u on s.user_id = u.id
    """)
    List<SocializeVO> selectAll();
}
