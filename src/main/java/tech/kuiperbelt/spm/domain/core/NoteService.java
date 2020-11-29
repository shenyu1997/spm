package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.support.UserContextHolder;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.event.EventService;

import java.time.LocalDate;

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

    public Note takeNote(Note note) {
        note.setCreateDate(LocalDate.now());
        if(note.getWorkItem() != null) {
            note.setParentType(Note.ParentType.WORK_ITEM);
        } else if(note.getPhase() != null) {
            note.setParentType(Note.ParentType.PHASE);
        } else if(note.getProject() != null) {
            note.setParentType(Note.ParentType.PROJECT);
        }
        Note createNote = noteRepository.save(note);
        switch (note.getParentType()) {
            case PHASE:
                sendEvent(Event.PHASE_EXECUTION_NOTE_TAKEN, note);
                break;
            case PROJECT:
                sendEvent(Event.PROJECT_EXECUTION_NOTE_TAKEN, note);
                break;
            case WORK_ITEM:
                sendEvent(Event.ITEM_EXECUTION_NOTE_TAKEN, note);
                break;
        }

        return createNote;
    }

    public void deleteNote(Note note) {
        noteRepository.delete(note);
        sendEvent(Event.ITEM_EXECUTION_NOTE_DELETED, note);
    }

    private void sendEvent(String key, Note note) {
        Event.EventBuilder eventBuilder = Event.builder().key(key).source(note);
        switch (key) {
            case Event.ITEM_EXECUTION_NOTE_TAKEN:
            case  Event.ITEM_EXECUTION_NOTE_DELETED:
                eventBuilder.args(note.getWorkItem().getName());
                break;
            case  Event.PHASE_EXECUTION_NOTE_TAKEN:
                eventBuilder.args(note.getPhase().getName());
                break;
            case  Event.PROJECT_EXECUTION_NOTE_TAKEN:
                eventBuilder.args(note.getProject().getName());
                break;
            default:
                throw new IllegalArgumentException("Unsupported key");
        }
        eventService.emit(eventBuilder.build());
    }


}
