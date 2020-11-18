package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.common.UserContextHolder;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;

import java.time.LocalDateTime;

@Transactional
@Setter
@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventService eventService;

    public Note takeNote(WorkItem workItem, Note note) {
        Assert.notNull(workItem, "WorkItem can not be null");
        note.setWorkItem(workItem);

        Note createNote = noteRepository.save(note);

        sendEvent(Event.ITEM_EXECUTION_NOTE_TAKE, note);

        return createNote;
    }

    private void sendEvent(String key, Note note) {
        Event.EventBuilder eventBuilder = Event.builder().key(key).source(note);
        switch (key) {
            case Event.ITEM_EXECUTION_NOTE_TAKE:
                eventBuilder.args(note.getWorkItem().getName());
                break;
            default:
                throw new IllegalArgumentException("Unsupported key");
        }
        eventService.emit(eventBuilder.build());
    }
}
