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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MessageService {

    private static List<EventReceiveRule> receiveRules = new ArrayList<>();

    static {
        receiveRules.add(EventReceiveRule.builder()
                .type(Event.Type.EXECUTION_STATUS_CHANGED)
                .participant(true)
                .build());

        receiveRules.add(EventReceiveRule.builder()
                .type(Event.Type.PARTICIPANT_CHANGED)
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
        if(CollectionUtils.isEmpty(events)) {
            return;
        }
        log.debug("MessageService.bulkProcessEvent: {}", events);
        List<String> allCandidate = getAllCandidate();
        for(String upn: allCandidate) {
            List<Event> interestedEvent = new ArrayList<>();
            for(Event event : events) {
                if(isNotTriggerMan(event, upn) && matchRule(event, upn)) {
                    interestedEvent.add(event);
                }
            }
            if(!CollectionUtils.isEmpty(interestedEvent)) {
                sendMessageTo(interestedEvent, upn);
            }
        }
    }

    private boolean matchRule(Event event, String upn) {
        for(EventReceiveRule receiveRule: receiveRules) {
            BaseEntity sourceEntity = idMappingService.getEntity(event.getSource());
            if(receiveRule.evaluate(event, upn, sourceEntity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotTriggerMan(Event event, String upn) {
        return !Objects.equals(event.getTriggeredMan(), upn);
    }

    private void sendMessageTo(List<Event> interested, String upn) {
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
