package kuiperbelt.tech.spm.domain.core;

import kuiperbelt.tech.spm.common.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class MessageService {

    private static List<MessageReceiveRule> receiveRules = new ArrayList<>();

    static {
        receiveRules.add(MessageReceiveRule.builder()
                .type(Event.Type.EXECUTION_STATUS_CHANGED)
                .participant(true)
                .build());

        receiveRules.add(MessageReceiveRule.builder()
                .type(Event.Type.PARTICIPANT_CHANGED)
                .member(true)
                .build());
    }

    @Autowired
    private EntityService entityService;

    @Autowired
    private MessageRepository messageRepository;


    public void bulkProcessEvent(Queue<Event> events) {
        if(CollectionUtils.isEmpty(events)) {
            return;
        }
        log.debug("MessageService.bulkProcessEvent: {}", events);
        List<String> allCandidate = getAllCandidate();
        for(String upn: allCandidate) {
            List<Event> interested = new ArrayList<>();
            for(Event event : events) {
                if(isNotTriggerMan(event, upn) && matchRule(event, upn)) {
                    interested.add(event);
                }
            }
            sendMessageTo(interested, upn);
        }
    }

    private boolean matchRule(Event event, String upn) {
        for(MessageReceiveRule receiveRule: receiveRules) {
            BaseEntity sourceEntity = entityService.getEntity(event.getSource());
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
}
