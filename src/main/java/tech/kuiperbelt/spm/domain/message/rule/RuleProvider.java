package tech.kuiperbelt.spm.domain.message.rule;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class RuleProvider {
    private static List<Rule> rules = new LinkedList<>();

    static {
        // project.create/project.removed/project.canceled
        rules.add(Rule.builder()
                .eventKey("event.project.*")
                .isMilestone(true)
                .build());

        // event.project.owner.changed
        rules.add(Rule.builder()
                .eventKey("event.project.owner.changed")
                .isProjectOwner(true)
                .isProjectManager(true)
                .build());

        //event.project.manager.changed
        rules.add(Rule.builder()
                .eventKey("event.project.manager.changed")
                .isProjectOwner(true)
                .isProjectManager(true)
                .build());

        // all new/removed member will receive notify
        rules.add(Rule.builder()
                .eventKey("event.project.member.*")
                .isProjectManager(true)
                .eventArgs(2)
                .build());

        // event.project.properties.*.change
        rules.add(Rule.builder()
                .eventKey("event.project.properties.*.change")
                .belongToProjectMember(true)
                .build());
    }

    public List<Rule> getAllRules() {
        return rules;
    }
}
