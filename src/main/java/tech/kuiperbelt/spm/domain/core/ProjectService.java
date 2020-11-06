package tech.kuiperbelt.spm.domain.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tech.kuiperbelt.spm.common.AuditService;
import tech.kuiperbelt.spm.common.UserContextHolder;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Transactional
@Service
@RepositoryEventHandler
public class ProjectService {

    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

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
                .type(Event.Type.INFORMATION_CHANGED)
                .key(Event.EVENT_PROJECT_CREATED)
                .source(project.getId())
                .args(project.getName())
                .build());

        if(!StringUtils.isEmpty(project.getOwner())) {
            eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .key(Event.EVENT_PROJECT_OWNER_CHANGED)
                    .source(project.getId())
                    .args(project.getName(), project.getOwner())
                    .build());
        }

        // set manager if need
        if(!StringUtils.isEmpty(project.getManager())) {
            eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .key(Event.EVENT_PROJECT_MANAGER_CHANGED)
                    .source(project.getId())
                    .args(project.getName(), project.getManager())
                    .build());
        }

        if(!CollectionUtils.isEmpty(project.getMembers())) {
            project.getMembers().forEach(upn -> eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .key(Event.EVENT_PROJECT_MEMBER_ADDED)
                    .source(project.getId())
                    .args(upn, project.getName())
                    .build()));
        }
        eventService.endEmit();
    }

    @HandleAfterSave
    public void preHandleProjectSave(Project current) {
        Optional<Project> previousVersion = auditService.getPreviousVersion(current);
        previousVersion.ifPresent(previous -> {
            if(log.isDebugEnabled()) {
                log.debug("Previous: {}, current {}", previous, current);
            }
            // if 'name' is changed
            if(!Objects.equals(previous.getName(), current.getName())) {
                eventService.emit(Event.builder()
                        .type(Event.Type.INFORMATION_CHANGED)
                        .key(Event.EVENT_PROJECT_NAME_CHANGE)
                        .source(current.getId())
                        .args(previous.getName(), current.getName())
                        .build());
            }
            // if 'manager' is changed
            if(!Objects.equals(previous.getManager(), current.getManager())) {
                eventService.emit(Event.builder()
                        .type(Event.Type.PARTICIPANT_CHANGED)
                        .key(Event.EVENT_PROJECT_MANAGER_CHANGED)
                        .source(current.getId())
                        .args(current.getName(), current.getManager())
                        .build());
            }
            // if 'owner' is changed
            if(!Objects.equals(previous.getOwner(), current.getOwner())) {
                eventService.emit(Event.builder()
                        .type(Event.Type.PARTICIPANT_CHANGED)
                        .key(Event.EVENT_PROJECT_OWNER_CHANGED)
                        .source(current.getId())
                        .args(current.getName(), current.getOwner())
                        .build());
            }

            // if 'member' was removed
            Set<String> previousMembers = new HashSet<>(previous.getMembers());
            previousMembers.removeAll(new HashSet<>(current.getMembers()));
            previousMembers.forEach(upn -> eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .key(Event.EVENT_PROJECT_MEMBER_REMOVED)
                    .source(previous.getId())
                    .args(upn, previous.getName())
                    .build()));

            // if 'member' was added
            Set<String> currentMembers = new HashSet<>(current.getMembers());
            currentMembers.removeAll(new HashSet<>(previous.getMembers()));
            currentMembers.forEach(upn -> eventService.emit(Event.builder()
                    .type(Event.Type.PARTICIPANT_CHANGED)
                    .key(Event.EVENT_PROJECT_MEMBER_ADDED)
                    .source(current.getId())
                    .args(upn, current.getName())
                    .build()));

            eventService.endEmit();
        });
    }
}
