package tech.kuiperbelt.spm.domain.core.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.core.support.UserContext;
import tech.kuiperbelt.spm.domain.core.support.UserContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Transactional
@Service
public class EventService {

    public static final String PROPERTY_CHANGE_SET_NEW = "property.change.set.new";
    public static final String PROPERTY_CHANGE_CLEAN_OLD = "property.change.clean.old";
    public static final String PROPERTY_CHANGE_UPDATE = "property.change.update";
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

    @SuppressWarnings("rawtypes")
    private static Stream<?> flatArgs(Object o) {
        if (o instanceof List) {
            return ((List) o).stream();
        } else {
            return Stream.of(o);
        }
    }

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
        Assert.notNull(event, "Event can not be null");
        Object[] eventArgs = preProcessing(event.getArgs());
        String content = messageSource.getMessage(event.getKey(),
                eventArgs,
                LocaleContextHolder.getLocale());
        event.setDetail(content);
        return event;
    }

    private Object[] preProcessing(Object[] args) {
        List<Object> collection = Arrays.stream(args)
                .flatMap(EventService::flatArgs)
                .collect(Collectors.toList());

        List<Object> result = new ArrayList<>();
        List<String> propertiesChanged = new ArrayList<>();
        for(Object arg: collection) {
            if(PropertyChanged.compatibleWith(arg)) {
                propertiesChanged.add(toString(PropertyChanged.from(arg)));
            } else {
                if(!propertiesChanged.isEmpty()) {
                    result.add(String.join(", ", propertiesChanged));
                    propertiesChanged.clear();
                }
                result.add(arg);
            }
        }
        if(!propertiesChanged.isEmpty()) {
            result.add(String.join(", ", propertiesChanged));
            propertiesChanged.clear();
        }
        return result.toArray();
    }

    private String toString(PropertyChanged pc) {
        Locale locale = LocaleContextHolder.getLocale();
        if(!pc.getOldValue().isPresent() && pc.getNewValue().isPresent()) {
            return messageSource.getMessage(PROPERTY_CHANGE_SET_NEW, new Object[]{ pc.getProperty(), pc.getNewValue().get()}, locale);
        } else if(!pc.getNewValue().isPresent()) {
            return messageSource.getMessage(PROPERTY_CHANGE_CLEAN_OLD, new Object[]{ pc.getProperty(), pc.getOldValue().get()}, locale);
        } else {
            return messageSource.getMessage(PROPERTY_CHANGE_UPDATE, new Object[]{ pc.getProperty(), pc.getOldValue().get(), pc.getNewValue().get()}, locale);
        }
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
