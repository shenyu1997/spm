package tech.kuiperbelt.spm.domain.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.common.BaseEntity;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;
import tech.kuiperbelt.spm.domain.idmapping.IdMappingService;
import tech.kuiperbelt.spm.domain.message.rule.ProjectEventReceiveRule;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MessageService {

    private static List<ProjectEventReceiveRule> receiveRules = new ArrayList<>();

    static {
        // project.create/project.removed/project.canceled
        receiveRules.add(ProjectEventReceiveRule.builder()
                .key("event.project.*")
                .participant(true)
                .build());

        // event.project.owner.changed
        receiveRules.add(ProjectEventReceiveRule.builder()
                .key("event.project.owner.changed")
                .owner(true)
                .manager(true)
                .build());

        //event.project.manager.changed
        receiveRules.add(ProjectEventReceiveRule.builder()
                .key("event.project.manager.changed")
                .owner(true)
                .manager(true)
                .build());

        // all new/removed member will receive notify
        receiveRules.add(ProjectEventReceiveRule.builder()
                .key("event.project.member.*")
                .manager(true)
                .args(2)
                .build());

        // event.project.properties.*.change
        receiveRules.add(ProjectEventReceiveRule.builder()
                .key("event.project.properties.*.change")
                .member(true)
                .build());
    }

    @Autowired
    private IdMappingService idMappingService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EventService eventService;


    public void bulkProcessEvent(Queue<Event> events) {
        if(log.isDebugEnabled()) {
            log.debug("MessageService.bulkProcessEvent: {}", events);
        }
        List<String> allCandidate = getAllCandidate();
        for(String upn: allCandidate) {
            List<Event> interestedEvents = new LinkedList<>();
            for(Event event : events) {
                if(isNotTriggerMan(event, upn) && matchRule(event, upn)) {
                    interestedEvents.add(event);
                }
            }
            if(!CollectionUtils.isEmpty(interestedEvents)) {
                sendMessageTo(interestedEvents, upn);
            }
        }
    }

    private boolean matchRule(Event event, String upn) {
        for(ProjectEventReceiveRule receiveRule: receiveRules) {
            Optional<? extends BaseEntity> sourceEntityOptional = idMappingService.findEntity(event.getSource());
            if(!sourceEntityOptional.isPresent()) {
                return false;
            }
            if(receiveRule.evaluate(event, upn, sourceEntityOptional.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotTriggerMan(Event event, String upn) {
        return !Objects.equals(event.getTriggeredMan(), upn);
    }

    private void sendMessageTo(List<Event> interested, String upn) {
        if(log.isDebugEnabled()) {
            log.debug("Send message to {}, events {}", upn, interested);
        }
        Message message = Message.builder()
                .receiver(upn)
                .events(interested)
                .build();
        messageRepository.save(message);
    }

    private List<String> getAllCandidate() {
        return Arrays.asList("yu.shen","huanhuan.dong","yongjian.sha");
    }

    public List<Event> findMessageEvents(Long id) {
        return messageRepository.findById(id)
                .map(Message::getEvents)
                .orElse(Collections.emptyList())
                .stream()
                .map(eventService::enhance)
                .collect(Collectors.toList());
    }
}
