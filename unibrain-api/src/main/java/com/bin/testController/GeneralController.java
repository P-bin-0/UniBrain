package com.bin.testController;
import com.bin.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GeneralController {
    // 1. 返回普通对象
    @GetMapping("/data")
    public String getData() {
        return "Hello, World!";
    }

    // 2. 返回自定义对象
    @GetMapping("/user")
    public User getUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        return user;
    }

    // 3. 返回列表
    @GetMapping("/list")
    public java.util.List<String> getList() {
        return java.util.Arrays.asList("A", "B", "C");
    }

    // 4. 手动返回 Result（特殊情况）
    @GetMapping("/custom")
    public ApiResponse<String> getCustomResult() {
        return ApiResponse.success("Custom Result");
    }

    // 5. 测试无返回值
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem() {
        // 业务逻辑
    }

    // 内部类，仅用于示例
    static class User {
        private Long id;
        private String name;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
