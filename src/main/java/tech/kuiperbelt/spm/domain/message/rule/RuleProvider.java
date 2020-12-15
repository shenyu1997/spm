package tech.kuiperbelt.spm.domain.message.rule;

import org.springframework.stereotype.Service;
import tech.kuiperbelt.spm.domain.core.RunningStatus;
import tech.kuiperbelt.spm.domain.core.event.Event;

import java.util.LinkedList;
import java.util.List;

@Service
public class RuleProvider {
    private static List<Rule> rules = new LinkedList<>();

    static {
        // Project related rule
        rules.add(Rule.builder()
                .eventKey("event.project.*")
                .belongToProjectParticipant(true)
                .build());

        rules.add(Rule.builder()
                .eventKey(Event.PROJECT_OWNER_CHANGED)
                .belongToProjectParticipant(true)
                .build());

        rules.add(Rule.builder()
                .eventKey(Event.PROJECT_MANAGER_CHANGED)
                .belongToProjectParticipant(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.project.member.*")
                .isProjectManager(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.project.member.*")
                .eventArgs(2)
                .build());

        rules.add(Rule.builder()
                .eventKey(Event.PROJECT_PROPERTIES_CHANGED)
                .belongToProjectParticipant(true)
                .build());

        // Phase related rule
        rules.add(Rule.builder()
                .eventKey("event.phase.*")
                .isProjectManager(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.phase.*.*")
                .isProjectManager(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.phase.[^item].*")
                .projectStatus(RunningStatus.RUNNING)
                .belongToProjectParticipant(true)
                .build());


        rules.add(Rule.builder()
                .eventKey("event.phase.*")
                .projectStatus(RunningStatus.RUNNING)
                .belongToProjectParticipant(true)
                .build());


        // Normal workItem rules
        rules.add(Rule.builder()
                .eventKey("event.phase.item.*")
                .isWorkItemOwner(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.phase.item.*")
                .isWorkItemAssignee(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.phase.item.*")
                .isProjectManager(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*")
                .isProjectManager(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*")
                .isWorkItemAssignee(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*")
                .isWorkItemOwner(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*.*")
                .isProjectManager(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*.*")
                .isWorkItemAssignee(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*.*")
                .isWorkItemOwner(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.phase.item.*")
                .isMilestone(true)
                .projectStatus(RunningStatus.RUNNING)
                .isProjectOwner(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*")
                .isMilestone(true)
                .projectStatus(RunningStatus.RUNNING)
                .isProjectOwner(true)
                .build());

        rules.add(Rule.builder()
                .eventKey("event.item.*.*")
                .isMilestone(true)
                .projectStatus(RunningStatus.RUNNING)
                .isProjectOwner(true)
                .build());


    }

    public List<Rule> getAllRules() {
        return rules;
    }
}
