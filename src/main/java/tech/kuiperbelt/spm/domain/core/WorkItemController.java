package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
