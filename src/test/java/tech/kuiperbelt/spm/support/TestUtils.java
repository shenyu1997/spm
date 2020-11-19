package tech.kuiperbelt.spm.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;

import javax.persistence.EntityManager;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AllArgsConstructor
public class TestUtils {
    public static final String LOCATION = "location";

    private final  MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public String createRandomProject() throws Exception {
        Project newProject = new Project().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .build();

        String href = mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProject)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
        return href;
    }

    public String appendRandomPhase(String newProjectHref,	 LocalDate plannedEndDate) throws Exception {
        return appendRandomPhase(newProjectHref, null, plannedEndDate);
    }

    public String appendRandomPhase(String newProjectHref,
                                     LocalDate plannedStartDate,
                                     LocalDate plannedEndDate) throws Exception {
        Phase phase = new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(6))
                .plannedEndDate(plannedEndDate)
                .plannedStartDate(plannedStartDate)
                .build();

        String newPhaseHref = mockMvc.perform(post(newProjectHref + "/phases/actions/append")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
        return newPhaseHref;
    }
}
