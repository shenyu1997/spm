package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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
        noteRepository.delete(note);
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
