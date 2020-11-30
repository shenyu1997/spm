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

    @GetMapping("/{id}/notes")
    public ResponseEntity<CollectionModel<PersistentEntityResource>> getNotes(@PathVariable("id") Long id,
                                                                              PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        List<PersistentEntityResource> collection = phaseService.getNotes(id)
                .stream()
                .map(persistentEntityResourceAssembler::toModel)
                .collect(Collectors.toList());
        CollectionModel<PersistentEntityResource> collectionModel = CollectionModel.of(collection);
        collectionModel.add(linkTo(methodOn(PhaseController.class)
                .getNotes(id, persistentEntityResourceAssembler)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    public static class PhaseRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Phase>> {

        @Override
        public EntityModel<Phase> process(EntityModel<Phase> model) {
            Optional<Link> self = model.getLink("self");
            self.ifPresent(selfLink -> {
                model.add(Link.of(selfLink.getHref() + "/notes", "notes"));
            });
            return model;
        }
    }

}
