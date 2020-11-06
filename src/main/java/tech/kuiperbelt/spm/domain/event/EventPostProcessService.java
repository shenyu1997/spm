package tech.kuiperbelt.spm.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.message.MessageService;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Transactional
@Service
public class EventPostProcessService {
    public static final int EVENTS_MAX_WINDOW = 5000;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private MessageSource messageSource;

    private Map<String, Queue<Event>> eventMap = new ConcurrentHashMap<>();

    @TransactionalEventListener
    public void receiveEvent(Event event) {
        final String correlationId = event.getCorrelationId();
        Queue<Event> events = eventMap.computeIfAbsent(correlationId, this::newEventQueue);
        if(Event.Type.SYSTEM_BULK_END == event.getType()) {
            processingEvents(eventMap.remove(correlationId), false);
        } else {
            events.add(event);
        }
    }

    private Queue<Event> newEventQueue(String correlationId) {
        taskScheduler.schedule(() ->
                        processingEvents(eventMap.remove(correlationId), true),
                fallbackTime());
        return new ConcurrentLinkedQueue<>();
    }

    private void processingEvents(final Queue<Event> events, boolean isFallback) {
        if(CollectionUtils.isEmpty(events)) {
            return;
        }
        if(isFallback) {
            log.warn("EventPostProcessService.processingEvents executed caused by fallback, leek events: {}",
                    Arrays.toString(events.toArray()));
        }
        taskExecutor.execute(() -> {
            eventRepository.saveAll(events);
            messageService.bulkProcessEvent(events);
        });
    }

    private Date fallbackTime() {
        return new Date(System.currentTimeMillis() + EVENTS_MAX_WINDOW);
    }
}
