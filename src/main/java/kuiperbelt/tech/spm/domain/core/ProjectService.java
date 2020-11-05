package kuiperbelt.tech.spm.domain.core;

import com.google.common.collect.Lists;
import kuiperbelt.tech.spm.common.UserContextHolder;
import kuiperbelt.tech.spm.domain.event.Event;
import kuiperbelt.tech.spm.domain.event.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional
@Service
@RepositoryEventHandler
public class ProjectService {

    public static final String EVENT_PROJECT_CREATED = "event.project.created";
    public static final String EVENT_PROJECT_OWNER_CHANGED = "event.project.owner.changed";
    public static final String EVENT_PROJECT_MANAGER_CHANGED = "event.project.manager.changed";
    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventService eventService;

    @HandleBeforeCreate
    public void preHandleProjectCreate(Project project) {
        //set owner if need
        String currentUserName = userContextHolder.getUserContext().getUpn();
        if(StringUtils.isEmpty(project.getOwner())) {
            project.setOwner(currentUserName);
        }

        // set manager if need
        if(StringUtils.isEmpty(project.getManager())) {
            project.setManager(currentUserName);
        }

        // set status
        project.setStatus(RunningStatus.INIT);
    }

    @HandleAfterCreate
    public void postHandleProjectCreate(Project project) {
        eventService.emit(Event.builder()
                .type(Event.Type.EXECUTION_STATUS_CHANGED)
                .subType(EVENT_PROJECT_CREATED)
                .source(project.getId())
                .args(Lists.newArrayList(project.getName()))
                .build());

        if(!StringUtils.isEmpty(project.getOwner())) {
            eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .subType(EVENT_PROJECT_OWNER_CHANGED)
                    .source(project.getId())
                    .args(Lists.newArrayList(project.getName(), project.getOwner()))
                    .build());
        }

        // set manager if need
        if(!StringUtils.isEmpty(project.getManager())) {
            eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .subType(EVENT_PROJECT_MANAGER_CHANGED)
                    .source(project.getId())
                    .args(Lists.newArrayList(project.getName(), project.getManager()))
                    .build());
        }

        eventService.endEmit();
    }
}
