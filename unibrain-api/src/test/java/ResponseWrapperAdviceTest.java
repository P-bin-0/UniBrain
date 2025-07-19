import com.bin.UniBrainApplication;
import com.bin.response.ResponseWrapperAdvice;
import com.bin.testController.GeneralController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeneralController.class) // 只加载 Controller
@Import(ResponseWrapperAdvice.class) // 手动导入跨模块的 @ControllerAdvice
@ContextConfiguration(classes = UniBrainApplication.class)
public class ResponseWrapperAdviceTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testStringResponse() throws Exception {
        mockMvc.perform(get("/api/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功")) // 修正为 "msg"
                .andExpect(jsonPath("$.data").value("Hello, World!"));
    }

    @Test
    public void testObjectResponse() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    public void testListResponse() throws Exception {
        mockMvc.perform(get("/api/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    public void testCustomResult() throws Exception {
        mockMvc.perform(get("/api/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Custom Result"));
    }

    @Test
    public void testNoContentResponse() throws Exception {
        mockMvc.perform(delete("/api/delete"))
                .andExpect(status().isNoContent());
    }
}
