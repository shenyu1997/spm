package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;
import java.time.Period;

import static org.hamcrest.MatcherAssert.assertThat;
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

    @Test
    public void testPeriod() {
        LocalDate now = LocalDate.now();
        Period p = Period.between(now.plusDays(11), now.plusDays(20));
        assertThat(p.getDays(), equalTo(9));
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void insertPhaseBeforeProjectStart() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));
        String workItemBHref = testUtils.createRandomWorkItem(phaseAHref, null, currentDay.plusDays(6));
        String workItemCHref = testUtils.createRandomWorkItem(phaseAHref, currentDay.plusDays(4), null);

        // verify workItems in phase A
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.ready", equalTo(false)))
                .andExpect(jsonPath("$._links.phase.href", equalTo(workItemAHref + "/phase")));

        mockMvc.perform(get(workItemAHref + "/phase"))
                .andExpect(jsonPath("$._links.self.href", equalTo(phaseAHref)));

        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(currentDay.plusDays(6).toString())));

        mockMvc.perform(get(workItemCHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(4).toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(currentDay.plusDays(10).toString())));

        // Prepared phase B
        String phaseBHref = testUtils.appendRandomPhase(projectHref, currentDay.plusDays(20));
        String workItemEHref = testUtils.createRandomWorkItem(phaseBHref, currentDay.plusDays(11), currentDay.plusDays(13));
        String workItemFHref = testUtils.createRandomWorkItem(phaseBHref, null, currentDay.plusDays(15));
        String workItemGHref = testUtils.createRandomWorkItem(phaseBHref, currentDay.plusDays(14), null);

        // verify phase B, workItems need not to verify because it is as same case as above.
        mockMvc.perform(get(phaseBHref))
                .andExpect(jsonPath("$.seq", equalTo(1)))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(11).toString())))
                .andExpect(jsonPath("$.plannedEndDate", equalTo(currentDay.plusDays(20).toString())));


        // Insert into A and B
        String phaseCHref = testUtils.insertRandomPhase(projectHref, 1, currentDay.plusDays(20));
        mockMvc.perform(get(projectHref + "/phases"))
                .andExpect(jsonPath("$._embedded.phases.length()", equalTo(3)));

        // Verify Phase C
        mockMvc.perform(get(phaseCHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(11).toString())))
                .andExpect(jsonPath("$.plannedEndDate", equalTo(currentDay.plusDays(20).toString())))
                .andExpect(jsonPath("$.seq", equalTo(1)))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        // Verify phase A not changed
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.toString())))
                .andExpect(jsonPath("$.plannedEndDate", equalTo(currentDay.plusDays(10).toString())))
                .andExpect(jsonPath("$.seq", equalTo(0)));

        // Verify phase B move to later
        mockMvc.perform(get(phaseBHref))
                .andExpect(jsonPath("$.seq", equalTo(2)))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(21).toString())))
                .andExpect(jsonPath("$.plannedEndDate", equalTo(currentDay.plusDays(30).toString())));

        // Verify workItems in phase B move to later
        mockMvc.perform(get(workItemEHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(21).toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(currentDay.plusDays(23).toString())));

        mockMvc.perform(get(workItemFHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(21).toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(currentDay.plusDays(25).toString())));

        mockMvc.perform(get(workItemGHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(24).toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(currentDay.plusDays(30).toString())));

        // Insert into at first
        String phaseDHref = testUtils.insertRandomPhase(projectHref, 0, currentDay.plusDays(10), currentDay.plusDays(20));

        // Verify phase count
        mockMvc.perform(get(projectHref + "/phases"))
                .andExpect(jsonPath("$._embedded.phases.length()", equalTo(4)));

        // Verify phase D
        mockMvc.perform(get(phaseDHref))
                .andExpect(jsonPath("$.seq", equalTo(0)))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(10).toString())))
                .andExpect(jsonPath("$.plannedEndDate", equalTo(currentDay.plusDays(20).toString())));


        // Verify phase A move left
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.plusDays(21).toString())))
                .andExpect(jsonPath("$.plannedEndDate", equalTo(currentDay.plusDays(31).toString())))
                .andExpect(jsonPath("$.seq", equalTo(1)));

    }
    @Sql({"/cleanup.sql"})
    @Test
    public void insertPhaseBeforeAStartedPhase() throws Exception {
        String projectHref = testUtils.createRandomProject();
        LocalDate current = LocalDate.now();
        testUtils.appendRandomPhase(projectHref, current, current.plusDays(10));
        testUtils.start(projectHref);

        Phase phase = new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(6))
                .plannedEndDate(current.plusDays(10))
                .seq(0)
                .build();

        mockMvc.perform(post(projectHref + "/phases/actions/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isBadRequest());

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
