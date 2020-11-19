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
    public void getPhases() throws Exception {
        mockMvc.perform(get("/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number", equalTo(0)));
    }

    @Test
    public void getProjectPhases() throws Exception {
        // Todo
    }

    @Test
    public void startPhaseByStartProject() throws Exception {
        //TODO
    }

    @Test
    public void startPhaseAutomaticallyAfterFirstInsertRunningProject() throws Exception {
        //TODO
    }

    @Test
    public void donePhaseAutomaticallyStartNextPhaseAndImpactProjectIsCanBeDone() throws Exception {
        //TODO
    }

    @Test
    public void stopPhaseCanNotBeUpdate() throws Exception {
        //TODO
    }

    @Test
    public void insertPhaseBeforeProjectStart() throws Exception {
        //TODO
    }

    @Test
    public void insertPhaseAfterProjectStart() throws Exception {
        //TODO
    }

    @Test
    public void changePhaseTimeFrame() throws Exception {
        //TODO
    }

    @Test
    public void insertPhaseWithWorkItems() throws Exception {
        //TODO
    }

    @Test
    public void donePhaseAfterAllWorkItemsStop() throws Exception {
        //TODO
    }

    @Test
    public void cancelPhaseCascadeCancelAllWorkItemsNonStop() throws Exception {
        //TODO
    }

    @Test
    public void deletePhaseCascadeDeleteAllWorkItems() throws Exception {
        //TODO
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
