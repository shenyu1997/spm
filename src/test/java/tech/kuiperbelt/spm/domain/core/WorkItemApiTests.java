package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = WorkItemApiTests.MOCK_UERR)
public class WorkItemApiTests extends ApiTest {
    public static final String MOCK_UERR = "sjdfs.ldjfds";

    @Sql({"/cleanup.sql"})
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

    @Sql({"/cleanup.sql"})
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

    @Sql({"/cleanup.sql"})
    @Test
    public void updateWorkItem() throws Exception {
        // update before done
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        // Start workItem
        testUtils.start(workItemAHref);

        // Patch update workItem
        WorkItem workItem = new WorkItem().toBuilder()
                .deadLine(currentDay.plusDays(6))
                .name(RandomStringUtils.randomAlphanumeric(10))
                .build();

        testUtils.patchUpdate(workItemAHref, workItem);

        // Verify
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(currentDay.toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(currentDay.plusDays(6).toString())))
                .andExpect(jsonPath("$.name", equalTo(workItem.getName())));

        // Update after done
        testUtils.done(workItemAHref);

        // Verify can not update any more
        mockMvc.perform(patch(workItemAHref)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isBadRequest());

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void deleteWorkItemImpactPhaseCanBeDone() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        testUtils.start(projectHref);
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        testUtils.delete(workItemAHref);
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void movePhase() throws Exception {

        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));

        // Prepared phase B
        String phaseBHref = testUtils.appendRandomPhase(projectHref, currentDay.plusDays(20));
        String workItemBHref = testUtils.createRandomWorkItem(phaseBHref, currentDay.plusDays(15), currentDay.plusDays(16));

        // Test init override status
        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.overflow", equalTo(false)));

        // start project and start phase A
        testUtils.start(projectHref);
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // verify canBeDone after move both fromPhase and toPhase
        Map<String, String> patchedWorkItem = Collections.singletonMap("phase", phaseAHref);
        testUtils.patchUpdate(workItemBHref, patchedWorkItem);

        super.yield();

        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        mockMvc.perform(get(phaseAHref + "/work-items"))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(workItemBHref)));


        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.overflow", equalTo(true)));
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void noPhaseWorkItemLiftCycle() throws Exception {
        LocalDate current = LocalDate.now();
        // from init -> done -> delete
        String workItemAHref = testUtils.createRandomWorkItem(current, current.plusDays(10));
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        testUtils.start(workItemAHref);
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
                .andExpect(jsonPath("$.actualStartDate", notNullValue()));

        testUtils.done(workItemAHref);
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.actualStartDate", notNullValue()))
                .andExpect(jsonPath("$.actualEndDate", notNullValue()));

        testUtils.delete(workItemAHref);
        mockMvc.perform(get(workItemAHref))
                .andExpect(status().isNotFound());

        // from init -> cancel -> delete
        String workItemBHref = testUtils.createRandomWorkItem(current, current.plusDays(10));
        testUtils.start(workItemBHref);
        testUtils.cancel(workItemBHref);
        mockMvc.perform(get(workItemBHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.cancelled", equalTo(true)));

        testUtils.delete(workItemBHref);
        mockMvc.perform(get(workItemBHref))
                .andExpect(status().isNotFound());

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void noPhaseWorkItemUpdate() {

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void deleteCascadeToNotes() {

    }

}
