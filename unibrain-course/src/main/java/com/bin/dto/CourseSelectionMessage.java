package com.bin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 选课消息体，用于 RabbitMQ 异步处理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSelectionMessage implements Serializable {
    /**
     * 学生 ID
     */
    private Long studentId;

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 消息生成时间（毫秒时间戳）
     */
    private Long timestamp;
    /**
     * 获取格式化时间
     */
    public LocalDateTime getLocalDateTime() {
        return timestamp != null ?
                LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.of("+8")) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CourseSelectionMessage that = (CourseSelectionMessage) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(courseId, that.courseId) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseId, timestamp);
    }
}
