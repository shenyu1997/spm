package kuiperbelt.tech.spm.domain.core;

import kuiperbelt.tech.spm.common.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
@RepositoryEventHandler
public class ProjectService {

    public static final String EVENT_PROJECT_CREATED = "PROJECT_CREATED";
    public static final String EVENT_PROJECT_OWNER_CHANGED = "PROJECT_OWNER_CHANGED";
    public static final String EVENT_PROJECT_MANAGER_CHANGED = "PROJECT_MANAGER_CHANGED";
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
                .build());

        if(!StringUtils.isEmpty(project.getOwner())) {
            eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .subType(EVENT_PROJECT_OWNER_CHANGED)
                    .source(project.getId())
                    .args(Collections.singletonList(project.getOwner()))
                    .build());
        }

        // set manager if need
        if(!StringUtils.isEmpty(project.getManager())) {
            eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .subType(EVENT_PROJECT_MANAGER_CHANGED)
                    .source(project.getId())
                    .args(Collections.singletonList(project.getManager()))
                    .build());
        }

        eventService.endEmit();
    }
}
