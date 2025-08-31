package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Grades;
import com.bin.dto.StudentSemesterAvgDTO;
import com.bin.dto.vo.GradesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成绩Mapper接口
 * @author bin
 */
@Mapper
public interface GradesMapper extends BaseMapper<Grades> {
    /**
     * 查询用户每个学期的平均成绩（根据用户名查询）
     * @param userName 用户名
     * @return 成绩列表
     */
    @Select("select user_name as userName,semester,ROUND(avg(grade),2) as avgGrade from grades where user_name=#{userName} group by semester")
    List<StudentSemesterAvgDTO> getAVG(String userName);
}
