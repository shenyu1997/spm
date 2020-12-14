package tech.kuiperbelt.spm.domain.message.rule;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.RunningStatus;
import tech.kuiperbelt.spm.domain.core.WorkItem;
import tech.kuiperbelt.spm.domain.core.event.Event;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleTest {

    private String upn;

    @BeforeEach
    void before() {
        upn = RandomStringUtils.randomAlphanumeric(10);
    }

    @Test
    void evaluateKey() {
        Rule rule1 = Rule.builder()
                .eventKey("event.test.*.*")
                .build();

        Event event1 = Event.builder()
                .key("event.test.a.b")
                .build();

        assertTrue(rule1.evaluate(event1, upn, null, null, null));

        //
        Rule rule2 = Rule.builder()
                .eventKey("event.test.*.*")
                .build();

        Event event2 = Event.builder()
                .key("event.test.a.b.c")
                .build();

        assertFalse(rule2.evaluate(event2, upn, null, null, null));

        //
        Rule rule3 = Rule.builder()
                .eventKey("event.test.*.*")
                .build();

        Event event3 = Event.builder()
                .key("event.c.a.b")
                .build();

        assertFalse(rule3.evaluate(event3, upn, null, null, null));

        //
        Rule rule4 = Rule.builder()
                .eventKey("event.test.a.c")
                .build();

        Event event4 = Event.builder()
                .key("event.test.a.c")
                .build();

        assertTrue(rule4.evaluate(event4, upn, null, null, null));

        //
        Rule rule5 = Rule.builder()
                .eventKey("event.test.a.c")
                .build();

        Event event5 = Event.builder()
                .key("event.test.s.c")
                .build();

        assertFalse(rule5.evaluate(event5, upn, null, null, null));

    }

    @Test
    public void evaluateTriggerMan() {
        // default
        Rule rule = Rule.builder().build();
        Event event = Event.builder().triggeredMan(RandomStringUtils.randomAlphanumeric(6)).build();
        assertTrue(rule.evaluate(event, upn, null, null, null));

        // triggerMan need match, suppose true
        Rule rule2 = Rule.builder().includeTriggerMan(true).build();
        Event event2 = Event.builder().triggeredMan(upn).build();
        assertTrue(rule2.evaluate(event2, upn, null, null, null));

        // triggerMan need match, suppose false
        Rule rule3 = Rule.builder().includeTriggerMan(true).build();
        Event event3 = Event.builder().triggeredMan(RandomStringUtils.randomAlphanumeric(10)).build();
        assertFalse(rule3.evaluate(event3, upn, null, null, null));

    }

    @Test
    public void evaluateEventArgs() {
        Rule rule = Rule.builder().eventArgs(1).build();
        Event event = Event.builder().args(RandomStringUtils.randomAlphanumeric(10), upn).build();
        assertTrue(rule.evaluate(event, upn, null, null, null));

        Rule rule2 = Rule.builder().eventArgs(2).build();
        Event event2 = Event.builder().args(RandomStringUtils.randomAlphanumeric(10), upn).build();
        assertFalse(rule2.evaluate(event2, upn, null, null, null));
    }

    @Test
    public void evaluateProjectOwner () {
        Rule rule1 = Rule.builder().isProjectOwner(true).build();
        Rule rule2 = Rule.builder().build();
        Rule rule3 = Rule.builder().isProjectOwner(false).build();

        Project project1 = new Project().toBuilder()
                .owner(upn)
                .build();

        Project project2 = new Project().toBuilder()
                .owner(RandomStringUtils.randomAlphanumeric(10))
                .build();

        Event event = Event.builder()
                .build();

        assertTrue(rule1.evaluate(event, upn, null, null, project1));
        assertTrue(rule2.evaluate(event, upn, null, null, project1));
        assertFalse(rule3.evaluate(event, upn, null, null, project1));

        assertFalse(rule1.evaluate(event, upn, null, null, project2));
        assertTrue(rule2.evaluate(event, upn, null, null, project2));
        assertTrue(rule3.evaluate(event, upn, null, null, project2));
    }

    @Test
    public void evaluateProjectManager () {
        Rule rule1 = Rule.builder().isProjectManager(true).build();
        Rule rule2 = Rule.builder().build();
        Rule rule3 = Rule.builder().isProjectManager(false).build();

        Project project1 = new Project().toBuilder()
                .manager(upn)
                .build();

        Project project2 = new Project().toBuilder()
                .manager(RandomStringUtils.randomAlphanumeric(10))
                .build();

        Event event = Event.builder()
                .build();

        assertTrue(rule1.evaluate(event, upn, null, null, project1));
        assertTrue(rule2.evaluate(event, upn, null, null, project1));
        assertFalse(rule3.evaluate(event, upn, null, null, project1));

        assertFalse(rule1.evaluate(event, upn, null, null, project2));
        assertTrue(rule2.evaluate(event, upn, null, null, project2));
        assertTrue(rule3.evaluate(event, upn, null, null, project2));
    }

    @Test
    public void evaluateProjectMembers () {
        Rule rule1 = Rule.builder().belongToProjectMember(true).build();
        Rule rule2 = Rule.builder().build();
        Rule rule3 = Rule.builder().belongToProjectMember(false).build();

        Project project1 = new Project().toBuilder()
                .members(Collections.singletonList(upn))
                .build();

        Project project2 = new Project().toBuilder()
                .members(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)))
                .build();

        Event event = Event.builder()
                .build();

        assertTrue(rule1.evaluate(event, upn, null, null, project1));
        assertTrue(rule2.evaluate(event, upn, null, null, project1));
        assertFalse(rule3.evaluate(event, upn, null, null, project1));

        assertFalse(rule1.evaluate(event, upn, null, null, project2));
        assertTrue(rule2.evaluate(event, upn, null, null, project2));
        assertTrue(rule3.evaluate(event, upn, null, null, project2));
    }

    @Test
    public void evaluateProjectParticipants () {
        Rule rule1 = Rule.builder().belongToProjectParticipant(true).build();
        Rule rule2 = Rule.builder().build();
        Rule rule3 = Rule.builder().belongToProjectParticipant(false).build();

        Project project1 = new Project().toBuilder()
                .members(Collections.singletonList(upn))
                .build();

        Project project2 = new Project().toBuilder()
                .members(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)))
                .build();

        Event event = Event.builder()
                .build();

        assertTrue(rule1.evaluate(event, upn, null, null, project1));
        assertTrue(rule2.evaluate(event, upn, null, null, project1));
        assertFalse(rule3.evaluate(event, upn, null, null, project1));

        assertFalse(rule1.evaluate(event, upn, null, null, project2));
        assertTrue(rule2.evaluate(event, upn, null, null, project2));
        assertTrue(rule3.evaluate(event, upn, null, null, project2));
    }

    @Test
    public void evaluateProjectStatus () {
        Rule rule1 = Rule.builder().projectStatus(RunningStatus.INIT).build();
        Rule rule2 = Rule.builder().build();

        Project project1 = new Project().toBuilder()
                .build();
        project1.initStatus();
        project1.start();

        Project project2 = new Project().toBuilder()
                .build();
        project2.initStatus();

        Event event = Event.builder()
                .build();

        assertFalse(rule1.evaluate(event, upn, null, null, project1));
        assertTrue(rule2.evaluate(event, upn, null, null, project1));


        assertTrue(rule1.evaluate(event, upn, null, null, project2));
        assertTrue(rule2.evaluate(event, upn, null, null, project2));

    }

    @Test
    public void evaluatePhaseStatus() {
        Rule rule1 = Rule.builder().phaseStatus(RunningStatus.INIT).build();
        Rule rule2 = Rule.builder().build();

        Phase phase1 = new Phase().toBuilder().build();
        phase1.initStatus();
        phase1.start();

        Phase phase2 = new Phase().toBuilder().build();
        phase2.initStatus();

        Event event = Event.builder().build();

        assertFalse(rule1.evaluate(event, upn, null, phase1, null));
        assertTrue(rule2.evaluate(event, upn, null, phase1, null));

        assertTrue(rule1.evaluate(event, upn, null, phase2, null));
        assertTrue(rule2.evaluate(event, upn, null, phase2, null));

    }

    @Test
    public void evaluateWorkItemStatus() {
        Rule rule1 = Rule.builder().workItemStatus(RunningStatus.INIT).build();
        Rule rule2 = Rule.builder().build();

        WorkItem workItem1 = new WorkItem().toBuilder().build();
        workItem1.initStatus();
        workItem1.start();

        WorkItem workItem2 = new WorkItem().toBuilder().build();
        workItem2.initStatus();

        Event event = Event.builder().build();

        assertFalse(rule1.evaluate(event, upn, workItem1, null , null));
        assertTrue(rule2.evaluate(event, upn, workItem1, null, null));

        assertTrue(rule1.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));

    }

    @Test
    public void evaluateWorkItemOwner() {
        Rule rule1 = Rule.builder().isWorkItemOwner(true).build();
        Rule rule2 = Rule.builder().isWorkItemOwner(false).build();
        Rule rule3 = Rule.builder().build();

        WorkItem workItem1 = new WorkItem().toBuilder()
                .owner(upn)
                .build();

        WorkItem workItem2 = new WorkItem().toBuilder().build();

        Event event = Event.builder().build();

        assertTrue(rule1.evaluate(event, upn, workItem1, null , null));
        assertFalse(rule2.evaluate(event, upn, workItem1, null, null));
        assertTrue(rule3.evaluate(event, upn, workItem1, null, null));

        assertFalse(rule1.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));

    }

    @Test
    public void evaluateWorkItemAssignee() {
        Rule rule1 = Rule.builder().isWorkItemAssignee(true).build();
        Rule rule2 = Rule.builder().isWorkItemAssignee(false).build();
        Rule rule3 = Rule.builder().build();

        WorkItem workItem1 = new WorkItem().toBuilder()
                .assignee(upn)
                .build();

        WorkItem workItem2 = new WorkItem().toBuilder().build();

        Event event = Event.builder().build();

        assertTrue(rule1.evaluate(event, upn, workItem1, null , null));
        assertFalse(rule2.evaluate(event, upn, workItem1, null, null));
        assertTrue(rule3.evaluate(event, upn, workItem1, null, null));

        assertFalse(rule1.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));

    }

    @Test
    public void evaluateWorkItemMilestone() {
        Rule rule1 = Rule.builder().isMilestone(true).build();
        Rule rule2 = Rule.builder().isMilestone(false).build();
        Rule rule3 = Rule.builder().build();

        WorkItem workItem1 = new WorkItem().toBuilder()
                .milestone(true)
                .build();

        WorkItem workItem2 = new WorkItem().toBuilder().build();

        Event event = Event.builder().build();

        assertTrue(rule1.evaluate(event, upn, workItem1, null , null));
        assertFalse(rule2.evaluate(event, upn, workItem1, null, null));
        assertTrue(rule3.evaluate(event, upn, workItem1, null, null));

        assertFalse(rule1.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));
        assertTrue(rule2.evaluate(event, upn, workItem2, null, null));

    }

}