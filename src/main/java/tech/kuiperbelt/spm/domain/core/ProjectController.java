package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.web.bind.annotation.*;

@Setter
@RepositoryRestController
@ResponseBody
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/{id}/actions/cancel")
    public void cancel(@PathVariable("id") long id) {
        projectService.cancelProject(id);
    }
}
