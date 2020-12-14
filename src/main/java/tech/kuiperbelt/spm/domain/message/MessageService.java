package tech.kuiperbelt.spm.domain.message;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.WorkItem;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;
import tech.kuiperbelt.spm.domain.message.rule.Rule;
import tech.kuiperbelt.spm.domain.message.rule.RuleProvider;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Setter
@Slf4j
@Service
@Transactional
public class MessageService {

    @Autowired
    private IdMappingService idMappingService;

    @Autowired
    private MessageRepository messageRepository;


    @Autowired
    private RuleProvider ruleProvider;


    @Async
    @EventListener
    public void bulkProcessEvent(Event.EventQueue events) {
        if(log.isDebugEnabled()) {
            log.debug("MessageService.bulkProcessEvent: {}", events);
        }
        List<String> allCandidate = getAllCandidate();
        for(String upn: allCandidate) {
            List<Event> interestedEvents = new LinkedList<>();
            for(Event event : events) {
                if(matchRule(event, upn)) {
                    interestedEvents.add(event);
                }
            }
            if(!CollectionUtils.isEmpty(interestedEvents)) {
                sendMessageTo(interestedEvents, upn);
            }
        }
    }

    private boolean matchRule(Event event, String upn) {
        Optional<? extends BaseEntity> sourceEntityOptional = idMappingService.findEntity(event.getSource());
        if(!sourceEntityOptional.isPresent()) {
            // return false in case no source entity, like internal event
            return false;
        }
        WorkItem workItem = null;
        Phase phase = null;
        Project project;
        BaseEntity sourceEntity = sourceEntityOptional.get();
        if(sourceEntity instanceof Project) {
            project = (Project) sourceEntity;
        } else if(sourceEntity instanceof Phase) {
            phase = (Phase) sourceEntity;
            project = phase.getProject();
        } else if(sourceEntity instanceof WorkItem) {
            workItem = (WorkItem) sourceEntity;
            phase = workItem.getPhase();
            project = workItem.getProject();
        } else {
            // Don't evaluate other type of source entity so far,
            // return false directly;
            return false;
        }
        for(Rule receiveRule: ruleProvider.getAllRules()) {
            if(receiveRule.evaluate(event, upn, workItem, phase, project)) {
                return true;
            }
        }
        return false;
    }

    private void sendMessageTo(List<Event> interested, String upn) {
        if(log.isDebugEnabled()) {
            log.debug("Send message to {}, events {}", upn, interested);
        }
        // Group by event source and combine to message
        interested.stream()
                .collect(Collectors.groupingBy(Event::getSource))
                .forEach((source, events) ->
                        messageRepository.save(Message.builder()
                            .receiver(upn)
                            .source(source)
                            .events(events.stream()
                                    .map(Event::getId)
                                    .collect(Collectors
                                            .toList()))
                            .build()));
    }

    private List<String> getAllCandidate() {
        return Arrays.asList("yu.shen","huanhuan.dong","yongjian.sha");
    }
}
