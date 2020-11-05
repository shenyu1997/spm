package kuiperbelt.tech.spm.domain.event;

import kuiperbelt.tech.spm.common.UserContext;
import kuiperbelt.tech.spm.common.UserContextHolder;
import kuiperbelt.tech.spm.domain.message.MessageService;
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
    private MessageService messageService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private MessageSource messageSource;

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
        eventRepository.save(event);

        postProcessEvent(event);
    }

    public void endEmit() {
        endEmit(userContextHolder.getUserContext().getCorrelationId());
    }

    public void endEmit(String correlationId) {
        Event endBulk = Event.builder()
                .type(Event.Type.SYSTEM_BULK_END)
                .correlationId(correlationId)
                .build();
        postProcessEvent(endBulk);
    }

    private void postProcessEvent(Event event) {
        final String correlationId = event.getCorrelationId();
        Queue<Event> events = eventMap.computeIfAbsent(correlationId, this::newEventQueue);
        if(Event.Type.SYSTEM_BULK_END == event.getType()) {
            sendToMessageService(eventMap.remove(correlationId));
        } else {
            events.add(event);
        }
    }

    private void sendToMessageService(Queue<Event> events) {
        messageService.bulkProcessEvent(events);
    }

    private Queue<Event> newEventQueue(String correlationId) {
        taskScheduler.schedule(() ->
                sendToMessageService(eventMap.remove(correlationId)),
                fallbackTime());
        return new ConcurrentLinkedQueue<>();
    }

    private Date fallbackTime() {
        return new Date(System.currentTimeMillis() + EVENTS_MAX_WINDOW);
    }

}
