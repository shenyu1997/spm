package tech.kuiperbelt.spm.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@SpringBootTest
public abstract class ApiTest {
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;


    protected TestUtils testUtils;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        testUtils = new TestUtils(this.mockMvc, this.objectMapper);
    }

    @SneakyThrows
    public void yield() {
        Thread.yield();
        Thread.sleep(10);
        Thread.yield();
    }

}
