package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.entity.Roles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RolesMapper extends BaseMapper<Roles> {
    @Select("select r.* from roles r inner join user_roles ur on r.id = ur.role_id where ur.user_id = #{userId}")
    List<Roles> getRolesByUserId(Long userId);
}
