package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Socialize;
import com.bin.dto.SocializeDTO;
import com.bin.dto.vo.SocializeVO;
import com.bin.mapper.SocializeMapper;
import com.bin.service.SocializeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 社交服务实现类
 */
@Service
public class SocializeServiceImpl extends ServiceImpl<SocializeMapper, Socialize> implements SocializeService {

    @Autowired
    private SocializeMapper socializeMapper;

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
    }

    /**
     * 搜索评论
     */
    @Override
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
    }

}