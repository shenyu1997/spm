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
    public void createWorkItem(@PathVariable("id") long id) {
        workItemService.startWorkItem(id);
    }

    @PostMapping("/{id}/actions/done")
    public void donePhase(@PathVariable("id") long id) {
        workItemService.doneWorkItem(id);
    }

    @PostMapping("/{id}/actions/cancel")
    public void cancelWorkItem(@PathVariable("id") long id) {
        workItemService.cancelWorkItem(id);
    }

    @PostMapping("/{id}/notes/actions/take-note")
    public ResponseEntity<Object> takeNote(@PathVariable("id") long id, @Valid @RequestBody Note note) {
        Note createdNote = workItemService.takeNote(id, note);
        URI uri = entityLinks.linkToItemResource(Note.class, createdNote.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

}
