package tech.kuiperbelt.spm.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.common.UserContext;
import tech.kuiperbelt.spm.common.UserContextHolder;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;

@Slf4j
@Transactional
@Service
public class EventService {

    public static final Locale FIX_LOCALE = Locale.CHINA;
    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private TaskExecutor taskExecutor;


    private ThreadLocal<Event.EventQueue> eventQueue = new ThreadLocal<>();

    @EventListener
    public void preparedEventQueue(Event.Signal signal) {
        if(signal != Event.BULK_BEGIN) {
            return;
        }
        eventQueue.set(new Event.EventQueue());
    }

    @TransactionalEventListener
    public void endBulk(Event.Signal signal) {
        if(signal != Event.BULK_END) {
            return;
        }
        if(eventQueue.get() != null) {
            postProcessingEvents(eventQueue.get());
            eventQueue.remove();
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void cleanEventQueue(Event.Signal signal) {
        if(signal != Event.BULK_END) {
            return;
        }
        // fallback if tx is rollback, can not use AFTER_COMPLETION,
        // because AFTER_COMPLETION will triggered before AFTER_COMMIT in normal case
        if(eventQueue.get() != null) {
            eventQueue.remove();
        }
    }

    public void emit(Event event) {
        Assert.notNull(event.getKey(), "Key must not null");
        Assert.notNull(event.getSource(), "Source must not null, event key:" + event.getKey());

        UserContext userContext = userContextHolder.getUserContext();
        event.setCorrelationId(userContext.getCorrelationId());
        event.setTriggeredMan(userContext.getUpn());
        event.setTimestamp(LocalDateTime.now());
        event.setUserContext(UserContext.of(userContext));
        eventQueue.get().add(event);
    }

    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id)
                .map(this::enhance);
    }

    public Event enhance(Event event) {
        String content = messageSource.getMessage(event.getKey(),
                event.getArgs(),
                FIX_LOCALE);
        event.setContent(content);
        return event;
    }

    private void postProcessingEvents(final Queue<Event> events) {
        if(CollectionUtils.isEmpty(events)) {
            return;
        }

        taskExecutor.execute(() -> {
            eventRepository.saveAll(events);
            // re-public events to other svc
            events.forEach(applicationEventPublisher::publishEvent);
            applicationEventPublisher.publishEvent(events);
        });
    }

}
