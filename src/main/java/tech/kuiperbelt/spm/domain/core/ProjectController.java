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
    public void cancel(@PathVariable("id") long id) {
        projectService.cancelProject(id);
    }

    @PostMapping("/{id}/actions/start")
    public void start(@PathVariable("id") long id) {
        projectService.startProject(id);
    }

    @PostMapping("/{id}/actions/done")
    public void done(@PathVariable("id") long id) {
        projectService.doneProject(id);
    }

    @PostMapping("/{id}/phases/actions/append")
    public ResponseEntity appendPhase(@PathVariable("id") Long id, @Valid @RequestBody Phase phase) {
        Project project = projectService.getProjectById(id);
        Phase createdPhase = phaseService.appendPhase(project, phase);
        URI uri = entityLinks.linkToItemResource(Phase.class, createdPhase.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/phases/actions/insert")
    public ResponseEntity insertPhase(@PathVariable("id") Long id, @Valid @RequestBody Phase phase) {
        Project project = projectService.getProjectById(id);
        Phase createdPhase = phaseService.insertPhase(project, phase);
        URI uri = entityLinks.linkToItemResource(Phase.class, createdPhase.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

}
