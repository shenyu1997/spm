package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = PhaseApiTests.MOCK_UERR)
public class PhaseApiTests extends ApiTest {
    public static final String MOCK_UERR = "sjdfs.ldjfds";

    @Sql({"/cleanup.sql"})
    @Test
    public void getPhases() throws Exception {
        mockMvc.perform(get("/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number", equalTo(0)));
    }
    @Sql({"/cleanup.sql"})
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
    @Sql({"/cleanup.sql"})
    @Test
    public void starProjectToDoneProject() throws Exception {
        String projectHref = testUtils.createRandomProject();
        String firstPhasesHref = testUtils.appendRandomPhase(projectHref, LocalDate.now(), LocalDate.now().plusDays(10));
        String secondPhaseHref = testUtils.appendRandomPhase(projectHref, LocalDate.now().plusDays(20));

        // 1. Start project
        testUtils.start(projectHref);

        // Verify project and phase status
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        mockMvc.perform(get(firstPhasesHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));

        mockMvc.perform(get(secondPhaseHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        // 2. Done first phase
        testUtils.done(firstPhasesHref);

        // Verify project and phase status
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        mockMvc.perform(get(firstPhasesHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())));

        mockMvc.perform(get(secondPhaseHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));

        // 3. Done second phase
        testUtils.done(secondPhaseHref);

        // Verify project and phase status
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        mockMvc.perform(get(secondPhaseHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())));

        // 4. Done project
        testUtils.done(projectHref);

        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())));

    }
    @Sql({"/cleanup.sql"})
    @Test
    public void startPhaseAutomaticallyAfterFirstInsertRunningProject() throws Exception {
        String projectHref = testUtils.createRandomProject();
        testUtils.start(projectHref);

        String firstPhasesHref = testUtils.appendRandomPhase(projectHref, LocalDate.now(), LocalDate.now().plusDays(10));
        mockMvc.perform(get(firstPhasesHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void stopPhaseCanNotBeUpdate() throws Exception {
        String projectHref = testUtils.createRandomProject();
        testUtils.start(projectHref);
        String firstPhasesHref = testUtils.appendRandomPhase(projectHref, LocalDate.now(), LocalDate.now().plusDays(10));

        // done the phase
        testUtils.done(firstPhasesHref);

        Phase phase = new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .build();
        mockMvc.perform(patch(firstPhasesHref)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isBadRequest());
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void insertPhaseBeforeProjectStart() throws Exception {
        //TODO
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void insertPhaseAfterProjectStart() throws Exception {
        //TODO
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void changePhaseTimeFrame() throws Exception {
        //TODO
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void insertPhaseWithWorkItems() throws Exception {
        //TODO
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void donePhaseAfterAllWorkItemsStop() throws Exception {
        //TODO
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void cancelPhaseCascadeCancelAllWorkItemsNonStop() throws Exception {
        //TODO
    }
    @Sql({"/cleanup.sql"})
    @Test
    public void deletePhaseCascadeDeleteAllWorkItems() throws Exception {
        //TODO
    }


}
