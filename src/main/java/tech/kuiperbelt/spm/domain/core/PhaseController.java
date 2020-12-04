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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
@RequestMapping("/phases")
public class PhaseController extends SpmRepositoryControllerSupport {

    @Lazy
    @Autowired
    private RepositoryEntityLinks entityLinks;

    @Autowired
    private PhaseService phaseService;

    @PostMapping("/{id}:{action}")
    public ResponseEntity<?> doAction(@PathVariable("id") long id,
                                      @PathVariable("action")ExecutableEntity.Action action) {
        switch (action) {
            case done:
                phaseService.donePhase(id);
                break;
            case delete:
                phaseService.deletePhase(id);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/work-items")
    public ResponseEntity<CollectionModel<?>> getWorkItems(@PathVariable("id") long id,
                                          PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        ResponseEntity<CollectionModel<?>> workItems = methodOn(PhaseController.class)
                .getWorkItems(id, persistentEntityResourceAssembler);

        return assembleCollectionResource(WorkItem.class, persistentEntityResourceAssembler,
                methodOn(PhaseController.class).getWorkItems(id, persistentEntityResourceAssembler),
                () -> phaseService.getWorkItems(id));
    }


    @PostMapping("/{id}/work-items")
    public ResponseEntity<?> createWorkItem(@PathVariable("id") long id, @Valid @RequestBody WorkItem workItem) {
        WorkItem createdWorkItem = phaseService.createWorkItem(id, workItem);
        URI uri = entityLinks.linkToItemResource(WorkItem.class, createdWorkItem.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<?> takeNote(@PathVariable("id") long id, @Valid @RequestBody Note note) {
        Note createdNote = phaseService.takeNote(id, note);
        URI uri = entityLinks.linkToItemResource(Note.class, createdNote.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<CollectionModel<?>> getNotes(@PathVariable("id") Long id,
                                                                              PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        return assembleCollectionResource(Note.class, persistentEntityResourceAssembler,
                methodOn(PhaseController.class).getNotes(id, persistentEntityResourceAssembler),
                () -> phaseService.getNotes(id));
    }

    public static class PhaseRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Phase>> {

        @Nonnull
        @Override
        public EntityModel<Phase> process(EntityModel<Phase> model) {
            Optional<Link> self = model.getLink("self");
            self.ifPresent(selfLink ->
                    model.add(Link.of(selfLink.getHref() + "/notes", "notes")));
            return model;
        }
    }

}
