package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import tech.kuiperbelt.spm.ApiTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = PhaseApiTests.MOCK_UERR)
public class PhaseApiTests extends ApiTest {
    public static final String MOCK_UERR = "sjdfs.ldjfds";
    public static final String LOCATION = "location";


    @Test
    public void getProjects() throws Exception {
        mockMvc.perform(get("/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number", equalTo(0)));
    }

    private String createRandomProject() throws Exception {
        Project newProject = new Project().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .build();

        return mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProject)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

}
