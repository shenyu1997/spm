package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@Setter
@RestController
@RequestMapping("/phases")
public class PhaseController {

    @Lazy
    @Autowired
    private RepositoryEntityLinks entityLinks;

    @Autowired
    private PhaseService phaseService;

    @PostMapping("/{id}/actions/done")
    public ResponseEntity<?> donePhase(@PathVariable("id") long id) {
        phaseService.donePhase(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/work-items/actions/create")
    public ResponseEntity<?> createWorkItem(@PathVariable("id") long id, @Valid @RequestBody WorkItem workItem) {
        WorkItem createdWorkItem = phaseService.createWorkItem(id, workItem);
        URI uri = entityLinks.linkToItemResource(WorkItem.class, createdWorkItem.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/notes/actions/take-note")
    public ResponseEntity<?> takeNote(@PathVariable("id") long id, @Valid @RequestBody Note note) {
        Note createdNote = phaseService.takeNote(id, note);
        URI uri = entityLinks.linkToItemResource(Note.class, createdNote.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

}
