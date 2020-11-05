package tech.kuiperbelt.spm.domain.event;

import tech.kuiperbelt.spm.common.UserContext;
import tech.kuiperbelt.spm.common.UserContextHolder;
import tech.kuiperbelt.spm.domain.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Transactional
@Service
public class EventService {

    public static final int EVENTS_MAX_WINDOW = 5000;
    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EventPostProcessService eventPostProcessService;

    private Map<String, Queue<Event>> eventMap = new ConcurrentHashMap<>();

    public void emit(Event event) {
        UserContext userContext = userContextHolder.getUserContext();
        event.setCorrelationId(userContext.getCorrelationId());
        event.setTriggeredMan(userContext.getUpn());
        event.setTimestamp(LocalDateTime.now());
        String content = messageSource.getMessage(event.getSubType(),
                event.getArgs().toArray(new String[0]),
                Locale.CHINA);
        event.setContent(content);

        eventPostProcessService.postProcessEvent(event);
    }

    public void endEmit() {
        endEmit(userContextHolder.getUserContext().getCorrelationId());
    }

    public void endEmit(String correlationId) {
        Event endBulk = Event.builder()
                .type(Event.Type.SYSTEM_BULK_END)
                .correlationId(correlationId)
                .build();
        eventPostProcessService.postProcessEvent(endBulk);
    }
}
