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

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
@RequestMapping("/projects")
public class ProjectController {

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

    @PostMapping("/{id}/phases/actions/append")
    public ResponseEntity<?> appendPhase(@PathVariable("id") Long id, @Valid @RequestBody Phase phase) {
        Phase createdPhase = projectService.appendPhase(id, phase);
        URI uri = entityLinks.linkToItemResource(Phase.class, createdPhase.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/phases/actions/insert")
    public ResponseEntity<?> insertPhase(@PathVariable("id") Long id, @Valid @RequestBody Phase phase) {
        Phase createdPhase = projectService.insertPhase(id, phase);
        URI uri = entityLinks.linkToItemResource(Phase.class, createdPhase.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/direct-work-items/actions/create")
    public ResponseEntity<?> createDirectWorkItem(@PathVariable("id") Long id, @Valid @RequestBody WorkItem workItem) {
        WorkItem createdWorkItem = projectService.createDirectWorkItem(id, workItem);
        URI uri = entityLinks.linkToItemResource(WorkItem.class, createdWorkItem.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}/direct-work-items")
    public ResponseEntity<CollectionModel<EntityModel>> getDirectWorkItems(@PathVariable("id") Long id, PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
        List<EntityModel> collection = projectService.getDirectWorkItems(id)
                .stream()
                .map(persistentEntityResourceAssembler::toModel)
                .collect(Collectors.toList());
        CollectionModel<EntityModel> collectionModel = CollectionModel.of(collection);
        collectionModel.add(linkTo(methodOn(ProjectController.class)
                .getDirectWorkItems(id, persistentEntityResourceAssembler)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    public static class ProjectRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Project>> {

        @Override
        public EntityModel<Project> process(EntityModel<Project> model) {
            Optional<Link> self = model.getLink("self");
            self.ifPresent(selfLink -> {
                model.add(Link.of(selfLink.getHref() + "/direct-work-items", "directWorkItems"));
            });
            return model;
        }
    }
}
