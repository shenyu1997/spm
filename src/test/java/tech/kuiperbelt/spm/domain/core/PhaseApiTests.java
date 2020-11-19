package tech.kuiperbelt.spm.domain.core;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = PhaseApiTests.MOCK_UERR)
public class PhaseApiTests extends ApiTest {
    public static final String MOCK_UERR = "sjdfs.ldjfds";

    @Test
    public void getPhases() throws Exception {
        mockMvc.perform(get("/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number", equalTo(0)));
    }

    @Test
    public void getProjectPhases() throws Exception {
        String projectHref = testUtils.createRandomProject();
        String firstPhasesHref = testUtils.appendRandomPhase(projectHref, LocalDate.now(), LocalDate.now().plusDays(10));
        String secondPhaseHref = testUtils.appendRandomPhase(projectHref, LocalDate.now().plusDays(20));
        mockMvc.perform(get(projectHref + "/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.phases.length()", equalTo(2)))
                .andExpect(jsonPath("$._embedded.phases..self.href",
                        hasItems(firstPhasesHref, secondPhaseHref)));
    }

    @Test
    public void starProjectToDoneProject() throws Exception {
        String projectHref = testUtils.createRandomProject();
        String firstPhasesHref = testUtils.appendRandomPhase(projectHref, LocalDate.now(), LocalDate.now().plusDays(10));
        String secondPhaseHref = testUtils.appendRandomPhase(projectHref, LocalDate.now().plusDays(20));

        // 1. Start project
        mockMvc.perform(post(projectHref + "/actions/start"))
                .andExpect(status().isNoContent());

        // Verify project and phase status
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        mockMvc.perform(get(firstPhasesHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));

        mockMvc.perform(get(secondPhaseHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        // 2. Done first phase
        mockMvc.perform(post(firstPhasesHref + "/actions/done"))
                .andExpect(status().isNoContent());

        // Verify project and phase status
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        mockMvc.perform(get(firstPhasesHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())));

        mockMvc.perform(get(secondPhaseHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));

        // 3. Done second phase
        mockMvc.perform(post(secondPhaseHref + "/actions/done"))
                .andExpect(status().isNoContent());

        // Verify project and phase status
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        mockMvc.perform(get(secondPhaseHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())));

        // 4. Done project
        mockMvc.perform(post(projectHref + "/actions/done"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())));

    }

    @Test
    public void startPhaseAutomaticallyAfterFirstInsertRunningProject() throws Exception {
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


}
