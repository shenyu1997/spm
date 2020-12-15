package tech.kuiperbelt.spm.domain.message.rule;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.WorkItem;
import tech.kuiperbelt.spm.domain.core.event.Event;

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
                .key(Event.PROJECT_CREATED)
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
                .key(Event.ITEM_OWNER_CHANGED)
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

    public boolean match(Event event, String upn, WorkItem workItem, Phase phase, Project project) {
        for(Rule rule: ruleProvider.getAllRules()) {
            if(rule.evaluate(event, upn, workItem, phase, project)) {
                return  true;
            }
        }
        return false;
    }

}