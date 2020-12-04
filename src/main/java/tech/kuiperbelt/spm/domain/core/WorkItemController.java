package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.kuiperbelt.spm.domain.core.support.ExecutableEntity;
import tech.kuiperbelt.spm.domain.core.support.SpmRepositoryControllerSupport;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
@RequestMapping("/" + WorkItemRepository.PATH_WORK_ITEMS)
public class WorkItemController extends SpmRepositoryControllerSupport {

    @Lazy
    @Autowired
    private RepositoryEntityLinks entityLinks;

    @Autowired
    private WorkItemService workItemService;

    @PostMapping("/{id}:{action}")
    public ResponseEntity<?> cancel(@PathVariable("id") long id,
                                    @PathVariable("action") ExecutableEntity.Action action) {
        switch (action) {
            case start:
                workItemService.startWorkItem(id);
                break;
            case cancel:
                workItemService.cancelWorkItem(id);
                break;
            case done:
                workItemService.doneWorkItem(id);
                break;
            case delete:
                workItemService.deleteWorkItem(id);
                break;
            default:
                throw new UnsupportedOperationException();

        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<?> takeNote(@PathVariable("id") long id, @Valid @RequestBody Note note) {
        Note createdNote = workItemService.takeNote(id, note);
        URI uri = entityLinks.linkToItemResource(Note.class, createdNote.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<CollectionModel<?>> getNotes(@PathVariable("id") Long id,
                                                                              PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        return assembleCollectionResource(persistentEntityResourceAssembler,
                methodOn(WorkItemController.class).getNotes(id, persistentEntityResourceAssembler),
                () -> workItemService.getNotes(id));
    }

    public static class WorkItemRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<WorkItem>> {


        @Nonnull
        @Override
        public EntityModel<WorkItem> process(EntityModel<WorkItem> model) {
            Optional<Link> self = model.getLink("self");
            self.ifPresent(selfLink ->
                    model.add(Link.of(selfLink.getHref() + "/notes", "notes")));
            return model;
        }
    }

}
