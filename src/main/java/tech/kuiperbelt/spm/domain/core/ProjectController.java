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
import tech.kuiperbelt.spm.domain.core.support.SpmRepositoryControllerSupport;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
@RequestMapping("/projects")
public class ProjectController extends SpmRepositoryControllerSupport {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PhaseService phaseService;

    @Lazy
    @Autowired
    private RepositoryEntityLinks entityLinks;

    @PostMapping("/{id}/actions/cancel")
    public ResponseEntity<?> cancel(@PathVariable("id") long id) {
        projectService.cancelProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/actions/start")
    public ResponseEntity<?> start(@PathVariable("id") long id) {
        projectService.startProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/actions/done")
    public ResponseEntity<?> done(@PathVariable("id") long id) {
        projectService.doneProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/phases")
    public ResponseEntity<CollectionModel<?>> getPhases(@PathVariable("id") Long id,
                                                                               PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        return assembleCollectionResource(persistentEntityResourceAssembler,
                methodOn(ProjectController.class).getPhases(id, persistentEntityResourceAssembler),
                () -> projectService.getAllPhases(id));
    }

    @PostMapping("/{id}/phases")
    public ResponseEntity<?> createPhase(@PathVariable("id") Long id, @Valid @RequestBody Phase phase) {
        Phase createdPhase = projectService.createPhase(id, phase);
        URI uri = entityLinks.linkToItemResource(Phase.class, createdPhase.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/direct-work-items")
    public ResponseEntity<?> createDirectWorkItem(@PathVariable("id") Long id, @Valid @RequestBody WorkItem workItem) {
        WorkItem createdWorkItem = projectService.createDirectWorkItem(id, workItem);
        URI uri = entityLinks.linkToItemResource(WorkItem.class, createdWorkItem.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}/direct-work-items")
    public ResponseEntity<CollectionModel<?>> getDirectWorkItems(@PathVariable("id") Long id,
                                                                           PersistentEntityResourceAssembler persistentEntityResourceAssembler) {

        return assembleCollectionResource(persistentEntityResourceAssembler,
                methodOn(ProjectController.class).getDirectWorkItems(id, persistentEntityResourceAssembler),
                () -> projectService.getDirectWorkItems(id));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<CollectionModel<?>> getNotes(@PathVariable("id") Long id,
                                                                 PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        return assembleCollectionResource(persistentEntityResourceAssembler,
                methodOn(ProjectController.class).getNotes(id, persistentEntityResourceAssembler),
                () -> projectService.getNotes(id));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<?> takeNote(@PathVariable("id") long id, @Valid @RequestBody Note note) {
        Note createdNote = projectService.takeNote(id, note);
        URI uri = entityLinks.linkToItemResource(Note.class, createdNote.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    public static class ProjectRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Project>> {

        @Nonnull
        @Override
        public EntityModel<Project> process(EntityModel<Project> model) {
            Optional<Link> self = model.getLink("self");
            self.ifPresent(selfLink -> {
                model.add(Link.of(selfLink.getHref() + "/direct-work-items", "directWorkItems"));
                model.add(Link.of(selfLink.getHref() + "/notes", "notes"));
            });
            return model;
        }
    }
}
