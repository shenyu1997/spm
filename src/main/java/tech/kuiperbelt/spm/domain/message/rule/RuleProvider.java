package tech.kuiperbelt.spm.domain.message.rule;

import org.springframework.stereotype.Service;
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
                .eventKey(Event.ITEM_OWNER_CHANGED)
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


    }

    public List<Rule> getAllRules() {
        return rules;
    }
}
