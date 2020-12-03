package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tech.kuiperbelt.spm.domain.core.event.Event;
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
        String workItemAHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

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
    public void projectAndPhaseCanBeDone() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();

        // Start project
        testUtils.start(projectHref);

        // Verify project can be done
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));

        // Verify project can be done, because of has a un-finished phase
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        // Verify phase can be done, because of no item in it.
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // add workItem
        String workItemAHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        // Verify phase can be done, because of has an item in it.
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        // Done workItem
        testUtils.start(workItemAHref);
        testUtils.done(workItemAHref);

        // Verify phase canBeDone
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // add workItem
        String workItemBHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        // Verify phase can be done, because of has an item in it again.
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        // Cancel workItem
        testUtils.cancel(workItemBHref);

        // Verify phase canBeDone, true again
        mockMvc.perform(get(phaseAHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // Done phase
        testUtils.done(phaseAHref);

        // Verify project can be done
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // Add project's workItem
        String workItemCHref = testUtils.createRandomProjectWorkItem(projectHref, currentDay, currentDay.plusDays(5));

        // Verify project can be done, false, because has an direct item
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        // Done workItem
        testUtils.start(workItemCHref);
        testUtils.done(workItemCHref);

        // Verify project can be done, false, because has no un-finished direct item
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // add workItem again
        String workItemDHref = testUtils.createRandomProjectWorkItem(projectHref, currentDay, currentDay.plusDays(5));

        // Verify project can be done, false, because has an direct item
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        testUtils.cancel(workItemDHref);
        // Verify project can be done, true, because has no un-finished direct item
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // add workItem again
        String workItemEHref = testUtils.createRandomProjectWorkItem(projectHref, currentDay, currentDay.plusDays(5));

        // Verify project can be done, false, because has an direct item
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        // Delete workItem
        testUtils.delete(workItemEHref);

        // Verify project can be done, true, because has no un-finished direct item
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void canBeDoneAfterWorkItemUpdatePhaseAndProject() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        testUtils.start(projectHref);

        // Verify project's canBeDone, suppose to be true
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

        // Add workItem in it
        String workItemHref = testUtils.createRandomProjectWorkItem(projectHref, currentDay, currentDay.plusDays(10));

        // Verify scope should be PROJECT
        mockMvc.perform(get(workItemHref))
                .andExpect(jsonPath("$.scope", equalTo(WorkItem.Scope.PROJECT.name())));

        // verify project's canBeDone, suppose to be false, because there is an direct workItem in it.
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(false)));

        // Move workItem out of project
        mockMvc.perform(delete(workItemHref + "/project"))
                .andExpect(status().isNoContent());

        // Verify scope change from PROJECT to PERSON
        mockMvc.perform(get(workItemHref))
                .andExpect(jsonPath("$.scope", equalTo(WorkItem.Scope.PERSON.name())));

        super.yield();

        // Verify project's canBeDone, suppose to be true again, because not direct item in it again
        mockMvc.perform(get(projectHref))
                .andExpect(jsonPath("$.canBeDone", equalTo(true)));

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void cancelAndDeleteProjectCascadeToDirectWorkItems() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();

        String workItemEHref = testUtils.createRandomProjectWorkItem(projectHref, currentDay, currentDay.plusDays(5));

        // Cancel project
        testUtils.cancel(projectHref);

        // Verify workItem is also canceled
        mockMvc.perform(get(workItemEHref))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.cancelled", equalTo(true)));

        // Delete project
        testUtils.delete(projectHref);

        // Verify workItem was deleted also
        mockMvc.perform(get(workItemEHref))
                .andExpect(status().isNotFound());

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void workItemLiftCycle() throws Exception {
        LocalDate currentDay = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        // Prepared phase A
        String phaseAHref = testUtils.appendRandomPhase(projectHref, currentDay, currentDay.plusDays(10));
        String workItemAHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));
        String workItemBHref = testUtils.createRandomPhaseWorkItem(phaseAHref, null, currentDay.plusDays(6));
        String workItemCHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay.plusDays(4), null);

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
        String workItemAHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

        // Verify workItem
        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.ready", equalTo(false)));

        // Start project and phase
        testUtils.start(projectHref);

        mockMvc.perform(get(workItemAHref))
                .andExpect(jsonPath("$.ready", equalTo(true)));

        // Add workItem to Running phase
        String workItemBHref = testUtils.createRandomPhaseWorkItem(phaseAHref, null, currentDay.plusDays(6));
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
        String workItemAHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

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
        String workItemAHref = testUtils.createRandomPhaseWorkItem(phaseAHref, currentDay, currentDay.plusDays(5));

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
        String workItemBHref = testUtils.createRandomPhaseWorkItem(phaseBHref, currentDay.plusDays(15), currentDay.plusDays(16));

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

    @Sql({"/cleanup.sql"})
    @Test
    public void testPropertiesChangedEvent() throws Exception {
        String workItemHref = testUtils.createRandomWorkItem(null, null);
        testUtils.verifyEvents(4,
                Event.ITEM_ADDED,
                Event.ITEM_OWNER_CHANGED,
                Event.ITEM_ASSIGNEE_CHANGED,
                Event.ITEM_READY_TRUE);
        testUtils.cleanAll("/events");
        testUtils.patchUpdate(workItemHref, Collections.singletonMap(WorkItem.Fields.owner,
                RandomStringUtils.randomAlphanumeric(10)));
        testUtils.verifyEvents(1, Event.ITEM_OWNER_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(workItemHref, Collections.singletonMap(WorkItem.Fields.assignee,
                RandomStringUtils.randomAlphanumeric(10)));
        testUtils.verifyEvents(1, Event.ITEM_ASSIGNEE_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(workItemHref, Collections.singletonMap(WorkItem.Fields.name,
                RandomStringUtils.randomAlphanumeric(10)));
        testUtils.verifyEvents(1, Event.ITEM_PROPERTIES_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(workItemHref, Collections.singletonMap(WorkItem.Fields.plannedStartDate,
                LocalDate.now()));
        testUtils.verifyEvents(1, Event.ITEM_START_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(workItemHref, Collections.singletonMap(WorkItem.Fields.deadLine,
                LocalDate.now()));
        testUtils.verifyEvents(1, Event.ITEM_END_CHANGED);

    }

    @Sql({"/cleanup.sql"})
    @Test
    public void testLifecycleEvent() throws Exception {
        String workItemHref = testUtils.createRandomWorkItem(null, null);

        testUtils.cleanAll("/events");
        testUtils.start(workItemHref);
        testUtils.verifyEvents(1, Event.ITEM_STARTED);

        testUtils.cleanAll("/events");
        testUtils.done(workItemHref);
        testUtils.verifyEvents(1, Event.ITEM_DONE);

        testUtils.cleanAll("/events");
        testUtils.delete(workItemHref);
        testUtils.verifyEvents(1, Event.ITEM_DELETED);

        String workItemBHref = testUtils.createRandomWorkItem(null, null);

        testUtils.cleanAll("/events");
        testUtils.cancel(workItemBHref);
        testUtils.verifyEvents(1, Event.ITEM_CANCELED);
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void testMoveProjectEvent() throws Exception {
        String projectHref = testUtils.createRandomProject();
        String itemHref = testUtils.createRandomWorkItem(null, null);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(itemHref, Collections.singletonMap(WorkItem.Fields.project, projectHref));
        testUtils.verifyEvents(1, Event.ITEM_PROJECT_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.delete(itemHref + "/project");
        testUtils.verifyEvents(1, Event.ITEM_PROJECT_CHANGED);
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void testMovePhaseEvent() throws Exception {
        LocalDate current = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        String phaseAHref = testUtils.appendRandomPhase(projectHref, current, current.plusDays(10));
        String phaseBHref = testUtils.appendRandomPhase(projectHref, current.plusDays(20));
        String itemHref = testUtils.createRandomWorkItem(null, null);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(itemHref, Collections.singletonMap(WorkItem.Fields.phase, phaseAHref));
        testUtils.verifyEvents(4,
                Event.ITEM_PHASE_CHANGED,
                Event.ITEM_PROJECT_CHANGED,
                Event.ITEM_START_CHANGED,
                Event.ITEM_END_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(itemHref, Collections.singletonMap(WorkItem.Fields.phase, phaseBHref));
        testUtils.verifyEvents(3,
                Event.ITEM_PHASE_CHANGED,
                Event.ITEM_START_CHANGED,
                Event.ITEM_END_CHANGED);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(phaseAHref, Collections.singletonMap(Phase.Fields.plannedEndDate, current.plusDays(6)));
        testUtils.verifyEvents(3,
                Event.PHASE_END_CHANGED,
                Event.PHASE_MOVED_LEFT,
                Event.ITEM_MOVED_LEFT);

        testUtils.cleanAll("/events");
        testUtils.patchUpdate(phaseAHref, Collections.singletonMap(Phase.Fields.plannedEndDate, current.plusDays(10)));
        testUtils.verifyEvents(3,
                Event.PHASE_END_CHANGED,
                Event.PHASE_MOVED_RIGHT,
                Event.ITEM_MOVED_RIGHT);
    }
}
