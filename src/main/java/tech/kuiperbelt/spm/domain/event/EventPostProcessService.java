package tech.kuiperbelt.spm.domain.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import tech.kuiperbelt.spm.domain.message.MessageService;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private MessageSource messageSource;

    private Map<String, Queue<Event>> eventMap = new ConcurrentHashMap<>();

    @Async
    @TransactionalEventListener
    public void postProcessEvent(Event event) {
        final String correlationId = event.getCorrelationId();
        Queue<Event> events = eventMap.computeIfAbsent(correlationId, this::newEventQueue);
        if(Event.Type.SYSTEM_BULK_END == event.getType()) {
            sendToMessageService(eventMap.remove(correlationId));
        } else {
            eventRepository.save(event);
            events.add(event);
        }
    }

    private Queue<Event> newEventQueue(String correlationId) {
        taskScheduler.schedule(() ->
                        sendToMessageService(eventMap.remove(correlationId)),
                fallbackTime());
        return new ConcurrentLinkedQueue<>();
    }

    private void sendToMessageService(Queue<Event> events) {
        messageService.bulkProcessEvent(events);
    }

    private Date fallbackTime() {
        return new Date(System.currentTimeMillis() + EVENTS_MAX_WINDOW);
    }
}
