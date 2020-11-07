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
        // all participant will notify INFORMATION_CHANGED
        receiveRules.add(EventReceiveRule.builder()
                .priority(0)
                .type(Event.Type.INFORMATION_CHANGED)
                .participant(true)
                .build());

        // all new/removed member will receive notify
        receiveRules.add(EventReceiveRule.builder()
                .priority(1)
                .type(Event.Type.PARTICIPANT_CHANGED)
                .key("event.project.member.*")
                .arg(0)
                .build());

        // all participants will care the project be canceled
        receiveRules.add(EventReceiveRule.builder()
                .priority(2)
                .participant(true)
                .key("event.project.*")
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
