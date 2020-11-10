package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Setter
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PhaseService phaseService;

    @PostMapping("/{id}/actions/cancel")
    public void cancel(@PathVariable("id") long id) {
        projectService.cancelProject(id);
    }

    @PostMapping("/{id}/phases/actions/append")
    public void appendPhase(@PathVariable("id") Long id, @Valid @RequestBody Phase phase) {
        phaseService.appendPhase(id, phase);
    }

}
