package com.bin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CourseDropMessage implements Serializable {
    private Long studentId;
    private Long courseId;
    private Long timestamp;

    public CourseDropMessage() {}

    public CourseDropMessage(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CourseDropMessage that = (CourseDropMessage) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(courseId, that.courseId) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseId, timestamp);
    }
}
