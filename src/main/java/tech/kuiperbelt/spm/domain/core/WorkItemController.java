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
@RequestMapping("/" + WorkItemRepository.PATH_WORK_ITEMS)
public class WorkItemController {

    @Lazy
    @Autowired
    private RepositoryEntityLinks entityLinks;

    @Autowired
    private WorkItemService workItemService;

    @PostMapping("/{id}/actions/start")
    public ResponseEntity<?> startWorkItem(@PathVariable("id") long id) {
        workItemService.startWorkItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/actions/done")
    public ResponseEntity<?> donePhase(@PathVariable("id") long id) {
        workItemService.doneWorkItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/actions/cancel")
    public ResponseEntity<?> cancelWorkItem(@PathVariable("id") long id) {
        workItemService.cancelWorkItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notes/actions/take-note")
    public ResponseEntity<?> takeNote(@PathVariable("id") long id, @Valid @RequestBody Note note) {
        Note createdNote = workItemService.takeNote(id, note);
        URI uri = entityLinks.linkToItemResource(Note.class, createdNote.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

}
