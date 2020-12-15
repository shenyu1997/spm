package tech.kuiperbelt.spm.domain.message.rule;

import com.google.common.collect.Sets;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.WorkItem;
import tech.kuiperbelt.spm.domain.core.event.Event;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleProviderTest {
    private RuleProvider ruleProvider = new RuleProvider();

    @Test
    void testProjectEvent() {
        String pOwner = RandomStringUtils.randomAlphanumeric(10);
        String pManger = RandomStringUtils.randomAlphanumeric(10);
        String pMemberA = RandomStringUtils.randomAlphanumeric(10);
        String pMemberB = RandomStringUtils.randomAlphanumeric(10);

        Project project = new Project().toBuilder()
                .owner(pOwner)
                .manager(pManger)
                .members(Lists.list(pMemberA, pMemberB))
                .build();

        // project create
        Event createEvent = Event.builder()
                .key(Event.PROJECT_ADDED)
                .source(project)
                .build();

        assertTrue(match(createEvent, pOwner, null, null, project));
        assertTrue(match(createEvent, pManger, null, null, project));
        assertTrue(match(createEvent, pMemberA, null, null, project));
        assertTrue(match(createEvent, pMemberB, null, null, project));
        assertFalse(match(createEvent, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // project delete

        Event delete = Event.builder()
                .key(Event.PROJECT_DELETED)
                .source(project)
                .build();

        assertTrue(match(delete, pOwner, null, null, project));
        assertTrue(match(delete, pManger, null, null, project));
        assertTrue(match(delete, pMemberA, null, null, project));
        assertTrue(match(delete, pMemberB, null, null, project));
        assertFalse(match(delete, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // project start
        Event start = Event.builder()
                .key(Event.PROJECT_STARTED)
                .source(project)
                .build();

        assertTrue(match(start, pOwner, null, null, project));
        assertTrue(match(start, pManger, null, null, project));
        assertTrue(match(start, pMemberA, null, null, project));
        assertTrue(match(start, pMemberB, null, null, project));
        assertFalse(match(start, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // project cancelled
        Event cancel = Event.builder()
                .key(Event.PROJECT_CANCELED)
                .source(project)
                .build();

        assertTrue(match(cancel, pOwner, null, null, project));
        assertTrue(match(cancel, pManger, null, null, project));
        assertTrue(match(cancel, pMemberA, null, null, project));
        assertTrue(match(cancel, pMemberB, null, null, project));
        assertFalse(match(cancel, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // project done
        Event done = Event.builder()
                .key(Event.PROJECT_DONE)
                .source(project)
                .build();

        assertTrue(match(done, pOwner, null, null, project));
        assertTrue(match(done, pManger, null, null, project));
        assertTrue(match(done, pMemberA, null, null, project));
        assertTrue(match(done, pMemberB, null, null, project));
        assertFalse(match(done, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // owner change
        Event ownerChanged = Event.builder()
                .key(Event.PROJECT_OWNER_CHANGED)
                .source(project)
                .triggeredMan(pOwner)
                .build();

        assertFalse(match(ownerChanged, pOwner, null, null, project)); // pOwner is trigger man
        assertTrue(match(ownerChanged, pManger, null, null, project));
        assertTrue(match(ownerChanged, pMemberA, null, null, project));
        assertTrue(match(ownerChanged, pMemberB, null, null, project));
        assertFalse(match(ownerChanged, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // manager change
        Event projectManager = Event.builder()
                .key(Event.PROJECT_MANAGER_CHANGED)
                .source(project)
                .triggeredMan(pOwner)
                .build();

        assertFalse(match(projectManager, pOwner, null, null, project)); // pOwner is trigger man
        assertTrue(match(projectManager, pManger, null, null, project));
        assertTrue(match(projectManager, pMemberA, null, null, project));
        assertTrue(match(projectManager, pMemberB, null, null, project));
        assertFalse(match(projectManager, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // member add
        String newMember = RandomStringUtils.randomAlphanumeric(10);
        project.setMembers(Lists.newArrayList(pMemberA, pMemberB, newMember));
        Event memberAdded = Event.builder()
                .key(Event.PROJECT_MEMBER_ADDED)
                .source(project)
                .triggeredMan(pManger)
                .args(project, "", Sets.newHashSet(newMember))
                .build();

        assertFalse(match(memberAdded, pOwner, null, null, project));
        assertFalse(match(memberAdded, pManger, null, null, project)); // pManager is trigger man
        assertFalse(match(memberAdded, pMemberA, null, null, project));
        assertFalse(match(memberAdded, pMemberB, null, null, project));
        assertTrue(match(memberAdded, newMember, null, null, project));
        assertFalse(match(memberAdded, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // member remove
        project.setMembers(Lists.newArrayList(pMemberA, pMemberB));
        Event memberRemoved = Event.builder()
                .key(Event.PROJECT_MEMBER_DELETED)
                .source(project)
                .triggeredMan(pManger)
                .args(project, "", Sets.newHashSet(newMember))
                .build();

        assertFalse(match(memberRemoved, pOwner, null, null, project));
        assertFalse(match(memberRemoved, pManger, null, null, project)); // pManager is trigger man
        assertFalse(match(memberRemoved, pMemberA, null, null, project));
        assertFalse(match(memberRemoved, pMemberB, null, null, project));
        assertTrue(match(memberRemoved, newMember, null, null, project));
        assertFalse(match(memberRemoved, RandomStringUtils.randomAlphanumeric(10), null, null, project));

        // properties change
        project.setName(RandomStringUtils.randomAlphanumeric(10));
        Event propertiesChange = Event.builder()
                .key(Event.PROJECT_PROPERTIES_CHANGED)
                .source(project)
                .triggeredMan(pManger)
                .args(project, "", Sets.newHashSet(newMember))
                .build();

        assertTrue(match(propertiesChange, pOwner, null, null, project));
        assertFalse(match(propertiesChange, pManger, null, null, project)); // pManager is trigger man
        assertTrue(match(propertiesChange, pMemberA, null, null, project));
        assertTrue(match(propertiesChange, pMemberB, null, null, project));
        assertFalse(match(propertiesChange, RandomStringUtils.randomAlphanumeric(10), null, null, project));
    }

    @Test
    public void testPhaseEventBeforeProjectStart() {
        String pOwner = RandomStringUtils.randomAlphanumeric(10);
        String pManger = RandomStringUtils.randomAlphanumeric(10);
        String pMemberA = RandomStringUtils.randomAlphanumeric(10);
        String pMemberB = RandomStringUtils.randomAlphanumeric(10);
        String pOther = RandomStringUtils.randomAlphanumeric(10);

        Project project = new Project().toBuilder()
                .owner(pOwner)
                .manager(pManger)
                .members(Lists.list(pMemberA, pMemberB))
                .build();
        project.initStatus();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        Phase phase = new Phase().toBuilder()
                .project(project)
                .plannedStartDate(end)
                .build();
        phase.initStatus();

        Event phaseAdded = Event.builder()
                .key(Event.PHASE_ADDED)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phaseAdded, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phaseAdded, pManger, null, phase, project)); //true, pManager is trigger man
        assertFalse(match(phaseAdded, pMemberA, null, phase, project)); //false, because project not start
        assertFalse(match(phaseAdded, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseAdded, pOther, null, phase, project));

        Event phaseAdded2 = Event.builder()
                .key(Event.PHASE_ADDED)
                .triggeredMan(pMemberA)
                .source(phase)
                .build();

        assertFalse(match(phaseAdded2, pOwner, null, phase, project));   // false, because project not start
        assertTrue(match(phaseAdded2, pManger, null, phase, project)); //true, pManager need aware phase added
        assertFalse(match(phaseAdded2, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phaseAdded2, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseAdded2, pOther, null, phase, project));

        Event phaseDeleted = Event.builder()
                .key(Event.PHASE_DELETED)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phaseDeleted, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phaseDeleted, pManger, null, phase, project)); // false, pManager need aware phase added
        assertFalse(match(phaseDeleted, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phaseDeleted, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseDeleted, pOther, null, phase, project)); //false

        Event phasePropertiesChanged = Event.builder()
                .key(Event.PHASE_PROPERTIES_CHANGED)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phasePropertiesChanged, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phasePropertiesChanged, pManger, null, phase, project)); // false, pManager need aware phase added
        assertFalse(match(phasePropertiesChanged, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phasePropertiesChanged, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phasePropertiesChanged, pOther, null, phase, project)); //false


        Event phaseMoveLeft = Event.builder()
                .key(Event.PHASE_MOVED_LEFT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phaseMoveLeft, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phaseMoveLeft, pManger, null, phase, project)); // false, pManager need aware phase added
        assertFalse(match(phaseMoveLeft, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phaseMoveLeft, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseMoveLeft, pOther, null, phase, project)); //false

        Event phaseMoveRight = Event.builder()
                .key(Event.PHASE_MOVED_RIGHT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phaseMoveRight, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phaseMoveRight, pManger, null, phase, project)); // false, pManager need aware phase added
        assertFalse(match(phaseMoveRight, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phaseMoveRight, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseMoveRight, pOther, null, phase, project)); //false

        Event phaseStartChanged = Event.builder()
                .key(Event.PHASE_MOVED_RIGHT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phaseStartChanged, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phaseStartChanged, pManger, null, phase, project)); // false, pManager need aware phase added
        assertFalse(match(phaseStartChanged, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phaseStartChanged, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseStartChanged, pOther, null, phase, project)); //false

        Event phaseEndChanged = Event.builder()
                .key(Event.PHASE_MOVED_RIGHT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertFalse(match(phaseEndChanged, pOwner, null, phase, project));   // false, because project not start
        assertFalse(match(phaseEndChanged, pManger, null, phase, project)); // false, pManager need aware phase added
        assertFalse(match(phaseEndChanged, pMemberA, null, phase, project)); //false, pMemberA is trigger man
        assertFalse(match(phaseEndChanged, pMemberB, null, phase, project)); //false, because project not start
        assertFalse(match(phaseEndChanged, pOther, null, phase, project)); //false

    }

    @Test
    public void testPhaseEventAfterProjectStart() {
        String pOwner = RandomStringUtils.randomAlphanumeric(10);
        String pManger = RandomStringUtils.randomAlphanumeric(10);
        String pMemberA = RandomStringUtils.randomAlphanumeric(10);
        String pMemberB = RandomStringUtils.randomAlphanumeric(10);
        String pOther = RandomStringUtils.randomAlphanumeric(10);

        Project project = new Project().toBuilder()
                .owner(pOwner)
                .manager(pManger)
                .members(Lists.list(pMemberA, pMemberB))
                .build();
        project.initStatus();
        project.start();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        Phase phase = new Phase().toBuilder()
                .project(project)
                .plannedStartDate(end)
                .build();
        phase.initStatus();

        Event phaseAdded = Event.builder()
                .key(Event.PHASE_ADDED)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phaseAdded, pOwner, null, phase, project));   // true, because project is started
        assertFalse(match(phaseAdded, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phaseAdded, pMemberA, null, phase, project)); // true, because project is started
        assertTrue(match(phaseAdded, pMemberB, null, phase, project)); // true, because project is started
        assertFalse(match(phaseAdded, pOther, null, phase, project));

        Event phaseAdded2 = Event.builder()
                .key(Event.PHASE_ADDED)
                .triggeredMan(pMemberA)
                .source(phase)
                .build();

        assertTrue(match(phaseAdded2, pOwner, null, phase, project));    // true, because project is started
        assertTrue(match(phaseAdded2, pManger, null, phase, project));  // false, pManager is trigger man
        assertFalse(match(phaseAdded2, pMemberA, null, phase, project));  // true, because project is started
        assertTrue(match(phaseAdded2, pMemberB, null, phase, project));  // true, because project is started
        assertFalse(match(phaseAdded2, pOther, null, phase, project));

        Event phaseDeleted = Event.builder()
                .key(Event.PHASE_DELETED)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phaseDeleted, pOwner, null, phase, project));   // true, because project not start
        assertFalse(match(phaseDeleted, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phaseDeleted, pMemberA, null, phase, project)); // true,  because project is start
        assertTrue(match(phaseDeleted, pMemberB, null, phase, project)); // true, because project is start
        assertFalse(match(phaseDeleted, pOther, null, phase, project)); //false

        Event phasePropertiesChanged = Event.builder()
                .key(Event.PHASE_PROPERTIES_CHANGED)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phasePropertiesChanged, pOwner, null, phase, project));   // true, because project not start
        assertFalse(match(phasePropertiesChanged, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phasePropertiesChanged, pMemberA, null, phase, project)); // true,  because project is start
        assertTrue(match(phasePropertiesChanged, pMemberB, null, phase, project)); // true, because project is start
        assertFalse(match(phasePropertiesChanged, pOther, null, phase, project)); //false


        Event phaseMoveLeft = Event.builder()
                .key(Event.PHASE_MOVED_LEFT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phaseMoveLeft, pOwner, null, phase, project));   // true, because project not start
        assertFalse(match(phaseMoveLeft, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phaseMoveLeft, pMemberA, null, phase, project)); // true,  because project is start
        assertTrue(match(phaseMoveLeft, pMemberB, null, phase, project)); // true, because project is start
        assertFalse(match(phaseMoveLeft, pOther, null, phase, project)); //false

        Event phaseMoveRight = Event.builder()
                .key(Event.PHASE_MOVED_RIGHT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phaseMoveRight, pOwner, null, phase, project));   // true, because project not start
        assertFalse(match(phaseMoveRight, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phaseMoveRight, pMemberA, null, phase, project)); // true,  because project is start
        assertTrue(match(phaseMoveRight, pMemberB, null, phase, project)); // true, because project is start
        assertFalse(match(phaseMoveRight, pOther, null, phase, project)); //false

        Event phaseStartChanged = Event.builder()
                .key(Event.PHASE_MOVED_RIGHT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phaseStartChanged, pOwner, null, phase, project));   // true, because project not start
        assertFalse(match(phaseStartChanged, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phaseStartChanged, pMemberA, null, phase, project)); // true,  because project is start
        assertTrue(match(phaseStartChanged, pMemberB, null, phase, project)); // true, because project is start
        assertFalse(match(phaseStartChanged, pOther, null, phase, project)); //false

        Event phaseEndChanged = Event.builder()
                .key(Event.PHASE_MOVED_RIGHT)
                .triggeredMan(pManger)
                .source(phase)
                .build();

        assertTrue(match(phaseEndChanged, pOwner, null, phase, project));   // true, because project not start
        assertFalse(match(phaseEndChanged, pManger, null, phase, project)); // false, pManager is trigger man
        assertTrue(match(phaseEndChanged, pMemberA, null, phase, project)); // true,  because project is start
        assertTrue(match(phaseEndChanged, pMemberB, null, phase, project)); // true, because project is start
        assertFalse(match(phaseEndChanged, pOther, null, phase, project)); //false

    }

    @Test
    public void normalPhaseWorkItem() {
        String pOwner = RandomStringUtils.randomAlphanumeric(10);
        String pManger = RandomStringUtils.randomAlphanumeric(10);
        String pMemberA = RandomStringUtils.randomAlphanumeric(10);
        String pMemberB = RandomStringUtils.randomAlphanumeric(10);
        String pOther = RandomStringUtils.randomAlphanumeric(10);

        Project project = new Project().toBuilder()
                .owner(pOwner)
                .manager(pManger)
                .members(Lists.list(pMemberA, pMemberB))
                .build();
        project.initStatus();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        Phase phase = new Phase().toBuilder()
                .project(project)
                .plannedStartDate(end)
                .build();
        phase.initStatus();

        WorkItem workItem = new WorkItem().toBuilder()
                .phase(phase)
                .scope(WorkItem.Scope.PHASE)
                .owner(pManger)
                .assignee(pMemberA)
                .build();
        workItem.initStatus();

        Event phaseItemAdded = Event.builder()
                .key(Event.PHASE_ITEM_ADDED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemAdded, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemAdded, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemAdded, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemAdded, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemAdded, pOther, workItem, phase, project)); //false

        Event phaseItemDelete = Event.builder()
                .key(Event.PHASE_ITEM_ADDED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemDelete, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemDelete, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemDelete, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemDelete, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemDelete, pOther, workItem, phase, project)); //false

        Event phaseItemStart = Event.builder()
                .key(Event.ITEM_STARTED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemStart, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemStart, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemStart, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemStart, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemStart, pOther, workItem, phase, project)); //false

        Event phaseItemDone = Event.builder()
                .key(Event.ITEM_DONE)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemDone, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemDone, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemDone, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemDone, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemDone, pOther, workItem, phase, project)); //false

        Event phaseItemCancelled = Event.builder()
                .key(Event.ITEM_CANCELED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemCancelled, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemCancelled, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemCancelled, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemCancelled, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemCancelled, pOther, workItem, phase, project)); //false

        Event phaseItemMoveLeft = Event.builder()
                .key(Event.ITEM_MOVED_LEFT)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemMoveLeft, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemMoveLeft, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemMoveLeft, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemMoveLeft, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemMoveLeft, pOther, workItem, phase, project)); //false

        Event phaseItemMoveRight = Event.builder()
                .key(Event.ITEM_MOVED_RIGHT)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemMoveRight, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemMoveRight, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemMoveRight, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemMoveRight, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemMoveRight, pOther, workItem, phase, project)); //false

        Event phaseItemStartChanged = Event.builder()
                .key(Event.ITEM_START_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemStartChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemStartChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemStartChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemStartChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemStartChanged, pOther, workItem, phase, project)); //false


        Event phaseItemEndChanged = Event.builder()
                .key(Event.ITEM_END_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemEndChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemEndChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemEndChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemEndChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemEndChanged, pOther, workItem, phase, project)); //false

        Event phaseItemPhaseChanged = Event.builder()
                .key(Event.ITEM_PHASE_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemPhaseChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemPhaseChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemPhaseChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemPhaseChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemPhaseChanged, pOther, workItem, phase, project)); //false

        Event phaseItemProjectChanged = Event.builder()
                .key(Event.ITEM_PROJECT_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertFalse(match(phaseItemProjectChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemProjectChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemProjectChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemProjectChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemProjectChanged, pOther, workItem, phase, project)); //false
    }

    @Test
    public void mileStonePhaseWorkItemInStartedProject() {
        String pOwner = RandomStringUtils.randomAlphanumeric(10);
        String pManger = RandomStringUtils.randomAlphanumeric(10);
        String pMemberA = RandomStringUtils.randomAlphanumeric(10);
        String pMemberB = RandomStringUtils.randomAlphanumeric(10);
        String pOther = RandomStringUtils.randomAlphanumeric(10);

        Project project = new Project().toBuilder()
                .owner(pOwner)
                .manager(pManger)
                .members(Lists.list(pMemberA, pMemberB))
                .build();
        project.initStatus();
        project.start();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        Phase phase = new Phase().toBuilder()
                .project(project)
                .plannedStartDate(end)
                .build();
        phase.initStatus();

        WorkItem workItem = new WorkItem().toBuilder()
                .phase(phase)
                .scope(WorkItem.Scope.PHASE)
                .owner(pManger)
                .assignee(pMemberA)
                .milestone(true)
                .build();
        workItem.initStatus();

        Event phaseItemAdded = Event.builder()
                .key(Event.PHASE_ITEM_ADDED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemAdded, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemAdded, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemAdded, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemAdded, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemAdded, pOther, workItem, phase, project)); //false

        Event phaseItemDelete = Event.builder()
                .key(Event.PHASE_ITEM_ADDED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemDelete, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemDelete, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemDelete, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemDelete, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemDelete, pOther, workItem, phase, project)); //false

        Event phaseItemStart = Event.builder()
                .key(Event.ITEM_STARTED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemStart, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemStart, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemStart, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemStart, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemStart, pOther, workItem, phase, project)); //false

        Event phaseItemDone = Event.builder()
                .key(Event.ITEM_DONE)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemDone, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemDone, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemDone, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemDone, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemDone, pOther, workItem, phase, project)); //false

        Event phaseItemCancelled = Event.builder()
                .key(Event.ITEM_CANCELED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemCancelled, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemCancelled, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemCancelled, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemCancelled, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemCancelled, pOther, workItem, phase, project)); //false

        Event phaseItemMoveLeft = Event.builder()
                .key(Event.ITEM_MOVED_LEFT)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemMoveLeft, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemMoveLeft, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemMoveLeft, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemMoveLeft, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemMoveLeft, pOther, workItem, phase, project)); //false

        Event phaseItemMoveRight = Event.builder()
                .key(Event.ITEM_MOVED_RIGHT)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemMoveRight, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemMoveRight, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemMoveRight, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemMoveRight, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemMoveRight, pOther, workItem, phase, project)); //false

        Event phaseItemStartChanged = Event.builder()
                .key(Event.ITEM_START_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemStartChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemStartChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemStartChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemStartChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemStartChanged, pOther, workItem, phase, project)); //false


        Event phaseItemEndChanged = Event.builder()
                .key(Event.ITEM_END_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemEndChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemEndChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemEndChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemEndChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemEndChanged, pOther, workItem, phase, project)); //false

        Event phaseItemPhaseChanged = Event.builder()
                .key(Event.ITEM_PHASE_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemPhaseChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemPhaseChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemPhaseChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemPhaseChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemPhaseChanged, pOther, workItem, phase, project)); //false

        Event phaseItemProjectChanged = Event.builder()
                .key(Event.ITEM_PROJECT_CHANGED)
                .source(workItem)
                .triggeredMan(pManger)
                .build();

        assertTrue(match(phaseItemProjectChanged, pOwner, workItem, phase, project));   // false,
        assertFalse(match(phaseItemProjectChanged, pManger, workItem, phase, project));  // false, pManager is trigger man
        assertTrue(match(phaseItemProjectChanged, pMemberA, workItem, phase, project));  // true,  because is assignee
        assertFalse(match(phaseItemProjectChanged, pMemberB, workItem, phase, project)); // false
        assertFalse(match(phaseItemProjectChanged, pOther, workItem, phase, project)); //false
    }

    public boolean match(Event event, String upn, WorkItem workItem, Phase phase, Project project) {
        for(Rule rule: ruleProvider.getAllRules()) {
            if(rule.evaluate(event, upn, workItem, phase, project)) {
                return  true;
            }
        }
        return false;
    }

}