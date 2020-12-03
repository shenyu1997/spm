package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.event.EventService;
import tech.kuiperbelt.spm.domain.core.support.UserContextHolder;

import java.time.LocalDate;
import java.util.List;

@Transactional
@Setter
@Service
@RepositoryEventHandler
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventService eventService;

    public List<Note> findByParent(Long parentId) {
        return noteRepository.findByParent(parentId);
    }

    public Note takeNote(Note note) {
        note.setCreateDate(LocalDate.now());
        return noteRepository.save(note);
    }

    public void deleteNote(Note note) {
        preHandlerDelete(note);
        noteRepository.delete(note);
        postHandlerDelete(note);

    }

    @HandleBeforeDelete
    public void preHandlerDelete(Note note) {
        //TODO should we check note parent's status before delete the note?
    }

    @HandleAfterDelete
    public void postHandlerDelete(Note note) {
        sendEvent(Event.NOTE_DELETED, note);
    }

    private void sendEvent(String key, Note note) {
        Event.EventBuilder eventBuilder = Event.builder().key(key).source(note);
        switch (key) {
            case  Event.NOTE_DELETED:
                eventBuilder.args(note.getContent());
                break;
            default:
                throw new IllegalArgumentException("Unsupported key");
        }
        eventService.emit(eventBuilder.build());
    }


    public void deleteNoteByParent(Long id) {
        noteRepository.deleteByParent(id);
    }
}
