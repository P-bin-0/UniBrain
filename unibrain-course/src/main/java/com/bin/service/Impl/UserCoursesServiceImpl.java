package com.bin.service.Impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.UserCourses;
import com.bin.dto.vo.UserCoursesVO;
import com.bin.mapper.UserCoursesMapper;
import com.bin.response.ApiResponse;
import com.bin.response.NoWrap;
import com.bin.service.UserCoursesService;

import com.bin.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserCoursesServiceImpl extends ServiceImpl<UserCoursesMapper, UserCourses> implements UserCoursesService {
    @Autowired
    private UserCoursesMapper userCoursesMapper;
    @Override
    public List<UserCoursesVO> getUserCourses() {
        // 获取当前用户的ID
        Long userId = SecurityUtil.getCurrentUserId();
        // 从数据库查询用户的课表
        return userCoursesMapper.getShowUserCourses(userId);
    }
    /**
     * 导出用户的课表为Excel文件
     */
    @Override
    public void exportScheduleToExcel(HttpServletResponse response) throws IOException {
        // 异常处理改进：使用try-catch包裹整个逻辑，并在异常时返回JSON响应
        try {
            String fileName = URLEncoder.encode("课程表", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + ".xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // 从数据库查询所有课程数据
            // 修改2：添加用户ID参数
            Long userId = SecurityUtil.getCurrentUserId();
            List<UserCoursesVO> dataList = userCoursesMapper.getShowUserCourses(userId);
            if (dataList == null) {
                dataList = new ArrayList<>(); // 使用可修改的空列表，避免Collections.emptyList()的问题
            }

            // 构建课节映射
            Map<Integer, UserCoursesVO> rowMap = new LinkedHashMap<>();
            // 修改1：使用可修改的ArrayList代替Arrays.asList()
            List<Integer> periods = new ArrayList<>(Arrays.asList(1, 2, 3, 4));//课节

            for (Integer period : periods) {
                UserCoursesVO row = new UserCoursesVO();
                row.setDayHour(period);
                row.setWeek("");
                row.setRoom("");
                row.setTeacher("");
                row.setCoursesName("");
                row.setUserName("");
                rowMap.put(period, row);
            }

            // 填充数据
            for (UserCoursesVO vo : dataList) {
                Integer period = vo.getDayHour();
                UserCoursesVO row = rowMap.get(period);
                if (row == null) continue;

                // 拼接单元格内容
                String cellContent = String.join("-",
                        Optional.ofNullable(vo.getCoursesName()).orElse(""),
                        Optional.ofNullable(vo.getTeacher()).orElse(""),
                        Optional.ofNullable(vo.getRoom()).orElse("")
                ).replaceAll("-+", "-").replaceAll("^-|-$", "");
            }

            // 转为EasyExcel可识别的结构
            List<List<String>> tableData = new ArrayList<>();
            // 第一行：表头
            tableData.add(Arrays.asList("节次", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"));

            // 后续行：每节课
            String[] weekArray = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
            Map<String, Integer> weekToIndex = IntStream.range(0, weekArray.length)
                    .boxed()
                    .collect(Collectors.toMap(i -> weekArray[i], i -> i + 1));

            for (Integer period : periods) {
                List<String> row = new ArrayList<>();
                row.add("第" + period + "节");
                // 初始化7天为空
                for (int i = 0; i < 7; i++) row.add("");

                // 填充该节次的所有课程
                for (UserCoursesVO vo : dataList) {
                    if (vo.getDayHour() != null && vo.getDayHour().equals(period)) {
                        Integer colIndex = weekToIndex.get(vo.getWeek());
                        if (colIndex != null) {
                            String content = String.join("-",
                                            Optional.ofNullable(vo.getCoursesName()).orElse(""),
                                            Optional.ofNullable(vo.getTeacher()).orElse(""),
                                            Optional.ofNullable(vo.getRoom()).orElse(""))
                                    .replaceAll("-+", "-").replaceAll("^-|-$", "");
                            row.set(colIndex, content);
                        }
                    }
                }
                tableData.add(row);
            }

            // 使用EasyExcel写出Excel
            try (ServletOutputStream out = response.getOutputStream()) {
                EasyExcel.write(out)
                        .head(createHeader())           // 自定义表头
                        .sheet("课程表")
                        .doWrite(buildExcelData(tableData));
            }
        } catch (Exception e) {
            // 异常处理改进：发生异常时返回JSON格式的错误响应
            response.setContentType("application/json;charset=UTF-8");
            ApiResponse<?> errorResponse = ApiResponse.error(500, "导出失败：" + e.getMessage());
            try {
                new ObjectMapper().writeValue(response.getWriter(), errorResponse);
            } catch (IOException ex) {
                log.error("导出课程表异常处理失败", ex);
            }
        }
    }

    // 构建EasyExcel所需的数据格式
    private List<List<String>> buildExcelData(List<List<String>> data) {
        return data.subList(1, data.size()); // 去掉第一行表头
    }

    // 创建表头
    private List<List<String>> createHeader() {
        List<List<String>> header = new ArrayList<>();
        // 修改2：将每个表头项都改为单独的列表项，且使用可修改的ArrayList
        header.add(new ArrayList<>(Collections.singletonList("节次")));
        header.add(new ArrayList<>(Collections.singletonList("星期一")));
        header.add(new ArrayList<>(Collections.singletonList("星期二")));
        header.add(new ArrayList<>(Collections.singletonList("星期三")));
        header.add(new ArrayList<>(Collections.singletonList("星期四")));
        header.add(new ArrayList<>(Collections.singletonList("星期五")));
        header.add(new ArrayList<>(Collections.singletonList("星期六")));
        header.add(new ArrayList<>(Collections.singletonList("星期日")));
        return header;
    }
}
