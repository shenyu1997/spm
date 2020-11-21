package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = WorkItemApiTests.MOCK_UERR)
public class WorkItemApiTests extends ApiTest {
    public static final String MOCK_UERR = "sjdfs.ldjfds";

    @Test
    public void workItemLiftCycle() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));
        String workItemBHref = testUtils.createRandomWorkItem(phaseAHref, null, currentDay.plusDays(6));
        String workItemCHref = testUtils.createRandomWorkItem(phaseAHref, currentDay.plusDays(4), null);

        // Verify init status of workItems
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        mockMvc.perform(get(workItemCHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));


        // Start A and B
        testUtils.start(workItemAHref);

        testUtils.start(workItemBHref);

        // Cancel C
        testUtils.cancel(workItemCHref);

        // Verify status
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));

        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));

        mockMvc.perform(get(workItemCHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.cancelled", equalTo(true)));

        // Done A
        testUtils.done(workItemAHref);

        // Cancel B
        testUtils.cancel(workItemBHref);

        // Verify
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.cancelled", equalTo(false)));

        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.cancelled", equalTo(true)));

        // Start project
        testUtils.start(projectHref);

        // Verify phase status and can be done
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

    }

    @Test
    public void workItemIsReady() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        // Verify workItem
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.ready", equalTo(false)));

        // Start project and phase
        testUtils.start(projectHref);

        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.ready", equalTo(true)));

        // Add workItem to Running phase
        String workItemBHref = testUtils.createRandomWorkItem(phaseAHref, null, currentDay.plusDays(6));
        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.ready", equalTo(true)));

        // Start and done workItems and then done phase
        testUtils.start(workItemAHref);
        testUtils.start(workItemBHref);
        testUtils.done(workItemAHref);
        testUtils.done(workItemBHref);
        testUtils.done(phaseAHref);

        // Verify can not add workItem again
        WorkItem workItem = new WorkItem().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .build();
        mockMvc.perform(post(phaseAHref + "/work-items/actions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateWorkItem() {
        // update before done

        // update after done
    }

    public void deleteWorkItemImpactPhaseCanBeDone() {
        //verify phase can be done
    }

    @Test
    public void deleteCascadeToNotes() {

    }

    @Test
    public void movePhase() {

        // verify canBeDone after move both fromPhase and toPhase
    }

    @Test
    public void noPhaseWorkItemLiftCycle() {

    }

    @Test
    public void noPhaseWorkItemUpdate() {

    }

}
