package com.bin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Grades;
import com.bin.dto.StudentSemesterAvgDTO;
import com.bin.dto.vo.GradesVO;
import com.bin.mapper.GradesMapper;
import com.bin.service.GradesService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成绩Service实现类
 * @author bin
 */
@Service
public class GradesServiceImpl extends ServiceImpl<GradesMapper, Grades> implements GradesService {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private GradesMapper gradesMapper;
    /**
     * 根据用户名查询成绩
     * @param userName 用户名
     * @return 成绩列表
     */
    @Override
    public List<GradesVO> getByUserName(String userName) {
        //判断用户名是否为空
        if (userName.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        //查询数据库中是否有该用户
        boolean exists = gradesMapper.exists(new LambdaQueryWrapper<Grades>().eq(Grades::getUserName, userName));
        if (!exists) {
            throw new IllegalArgumentException("用户不存在");
        }
        //查询该用户的成绩
        List<Grades> gradesList = gradesMapper.selectList(new LambdaQueryWrapper<Grades>().eq(Grades::getUserName, userName));
        //将查询结果转换为VO类并返回
        return BeanUtil.copyToList(gradesList, GradesVO.class);
    }

    /**
     * 查询用户每个学期的平均成绩（根据用户名查询）
     * @param userName 用户名
     * @return 成绩列表
     */
    @Override
    public List<StudentSemesterAvgDTO> getAverageGradesByUserName(String userName) {
        if (userName.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        //查询数据库中是否有该用户
        boolean exists = gradesMapper.exists(new LambdaQueryWrapper<Grades>().eq(Grades::getUserName, userName));
        if (!exists) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<StudentSemesterAvgDTO> list = gradesMapper.getAVG(userName);
        return list;
    }
    /**
     * 生成成绩分析报告
     * @param userName 用户名
     * @return 分析报告
     */
    @Override
    public String analyzeScoresWithAi(String userName) {
        //查询用户成绩
        List<StudentSemesterAvgDTO> scores = getAverageGradesByUserName(userName);
        //构建 Prompt：让 AI 自己分析！
        String prompt = """
                你是一名大学专业成绩分析员，请根据以下学生成绩数据，完成四项任务：
                请输出：
                 1. 成绩趋势分析
                 2. 薄弱知识点判断
                 3. 个性化学习建议（口语化、鼓励性，80字以内）
                 4. 之后适合的发展方向
                 请用中文输出，不要使用 Markdown，不要分点，直接写成一段话。
                """.formatted(scores);
        //调用 AI 模型
        try{
            return chatLanguageModel.chat(prompt);
        } catch (Exception e) {
            throw new RuntimeException("AI 分析暂时不可用，请稍后再试。", e);
        }
    }

}
