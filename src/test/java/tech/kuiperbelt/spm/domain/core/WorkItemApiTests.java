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
    public void relation() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        // Verify init status of workItems
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())))
                .andExpect(jsonPath("$.scope", equalTo(WorkItem.Scope.PHASE.name())));

        // Verify project href and phase href
        mockMvc.perform(get(workItemAHref + "/project"))
                .andExpect(jsonPath("$._links.self.href", equalTo(projectHref)));


        mockMvc.perform(get(workItemAHref + "/phase"))
                .andExpect(jsonPath("$._links.self.href", equalTo(phaseAHref)));

        // Verify project's work-items and phase's work-items
        mockMvc.perform(get(phaseAHref + "/work-items"))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(workItemAHref)));

        mockMvc.perform(get(projectHref + "/work-items"))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(workItemAHref)));

        // Remove project on workItem, and failed
        mockMvc.perform(delete(workItemAHref + "/project"))
                .andExpect(status().isBadRequest());

        // Remove phase on workItem
        mockMvc.perform(delete(workItemAHref + "/phase"))
                .andExpect(status().isNoContent());

        // Verify scope should be changed  to PROJECT
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.scope", equalTo(WorkItem.Scope.PROJECT.name())));

        // Verify workItem's phase not found
        mockMvc.perform(get(workItemAHref + "/phase"))
                .andExpect(status().isNotFound());

        // Verify workItems's project is still be found
        mockMvc.perform(get(workItemAHref + "/project"))
                .andExpect(jsonPath("$._links.self.href", equalTo(projectHref)));

        // Verify project href and phase href
        mockMvc.perform(get(phaseAHref + "/work-items"))
                .andExpect(jsonPath("$._embedded.workItems.length()", equalTo(0)));


        mockMvc.perform(get(projectHref + "/work-items"))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(workItemAHref)));


        // Remove project on workItem now, and successful
        mockMvc.perform(delete(workItemAHref + "/project"))
                .andExpect(status().isNoContent());

        // Verify scope change to PERSON
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.scope", equalTo(WorkItem.Scope.PERSON.name())));

        // Verify workItem's project not found
        mockMvc.perform(get(workItemAHref + "/project"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(projectHref + "/work-items"))
                .andExpect(jsonPath("$._embedded.workItems.length()", equalTo(0)));

    }

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
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())))
                .andExpect(jsonPath("$.scope", equalTo(WorkItem.Scope.PERSON.name())));

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
    public void noPhaseWorkItemUpdate() throws Exception {
        LocalDate current = LocalDate.now();
        // from init -> done -> delete
        String workItemAHref = testUtils.createRandomWorkItem(current, current.plusDays(10));
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));

        WorkItem workItem = new WorkItem().toBuilder()
                .detail(RandomStringUtils.randomAlphanumeric(10))
                .assignee(RandomStringUtils.randomAlphanumeric(6))
                .priority(WorkItem.Priority.HIGH)
                .plannedStartDate(current.plusDays(1))
                .deadLine(current.plusDays(5))
                .ready(true)
                .build();
        mockMvc.perform(patch(workItemAHref)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.detail", equalTo(workItem.getDetail())))
                .andExpect(jsonPath("$.assignee", equalTo(workItem.getAssignee())))
                .andExpect(jsonPath("$.priority", equalTo(workItem.getPriority().name())))
                .andExpect(jsonPath("$.plannedStartDate", equalTo(workItem.getPlannedStartDate().toString())))
                .andExpect(jsonPath("$.deadLine", equalTo(workItem.getDeadLine().toString())))
                .andExpect(jsonPath("$.ready", equalTo(workItem.getReady())));
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void deleteCascadeToNotes() throws Exception {
        LocalDate current = LocalDate.now();
        String workItemAHref = testUtils.createRandomWorkItem(current, current.plusDays(10));
        String noteARef = testUtils.taskRandomNote(workItemAHref);
        String noteBRef = testUtils.taskRandomNote(workItemAHref);
        testUtils.delete(workItemAHref);
        mockMvc.perform(get(noteARef)).andExpect(status().isNotFound());
        mockMvc.perform(get(noteBRef)).andExpect(status().isNotFound());
    }

}
