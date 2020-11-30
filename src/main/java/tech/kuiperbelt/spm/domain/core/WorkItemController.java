package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
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

    @GetMapping("/{id}/notes")
    public ResponseEntity<CollectionModel<PersistentEntityResource>> getNotes(@PathVariable("id") Long id,
                                                                              PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        List<PersistentEntityResource> collection = workItemService.getNotes(id)
                .stream()
                .map(persistentEntityResourceAssembler::toModel)
                .collect(Collectors.toList());
        CollectionModel<PersistentEntityResource> collectionModel = CollectionModel.of(collection);
        collectionModel.add(linkTo(methodOn(WorkItemController.class)
                .getNotes(id, persistentEntityResourceAssembler)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    public static class WorkItemRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<WorkItem>> {

        @Override
        public EntityModel<WorkItem> process(EntityModel<WorkItem> model) {
            Optional<Link> self = model.getLink("self");
            self.ifPresent(selfLink -> {
                model.add(Link.of(selfLink.getHref() + "/notes", "notes"));
            });
            return model;
        }
    }

}
