package tech.kuiperbelt.spm.domain.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.common.UserContext;
import tech.kuiperbelt.spm.common.UserContextHolder;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Transactional
@Service
public class EventService {

    public static final int EVENTS_MAX_WINDOW = 5000;
    public static final Locale FIX_LOCALE = Locale.CHINA;
    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    private Map<String, Queue<Event>> eventMap = new ConcurrentHashMap<>();

    public void emit(Event event) {
        UserContext userContext = userContextHolder.getUserContext();
        event.setCorrelationId(userContext.getCorrelationId());
        event.setTriggeredMan(userContext.getUpn());
        event.setTimestamp(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    public void endEmit() {
        endEmit(userContextHolder.getUserContext().getCorrelationId());
    }

    public void endEmit(String correlationId) {
        Event endBulk = Event.builder()
                .type(EventType.SYSTEM_BULK_END)
                .correlationId(correlationId)
                .build();
        emit(endBulk);
    }

    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id)
                .map(this::enhance);
    }

    public Event enhance(Event event) {
        String content = messageSource.getMessage(event.getType().key(),
                event.getArgs().toArray(new String[0]),
                FIX_LOCALE);
        event.setContent(content);
        return event;
    }
}
