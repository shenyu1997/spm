package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tech.kuiperbelt.spm.common.AuditService;
import tech.kuiperbelt.spm.common.UserContext;
import tech.kuiperbelt.spm.common.UserContextHolder;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;
import tech.kuiperbelt.spm.domain.event.EventType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Setter
@Slf4j
@Transactional
@Service
@RepositoryEventHandler()
public class ProjectService {

    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ProjectRepository projectRepository;

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
                .type(EventType.PROJECT_CREATED)
                .source(project.getId())
                .args(project.getName())
                .build());

        if(!StringUtils.isEmpty(project.getOwner())) {
            eventService.emit(Event.builder()
                    .type(EventType.PROJECT_OWNER_CHANGED)
                    .source(project.getId())
                    .args(project.getName(), project.getOwner())
                    .build());
        }

        // set manager if need
        if(!StringUtils.isEmpty(project.getManager())) {
            eventService.emit(Event.builder()
                    .type(EventType.PROJECT_MANAGER_CHANGED)
                    .source(project.getId())
                    .args(project.getName(), project.getManager())
                    .build());
        }

        if(!CollectionUtils.isEmpty(project.getMembers())) {
            project.getMembers().forEach(upn -> eventService.emit(Event.builder()
                    .type(EventType.PROJECT_MEMBER_ADDED)
                    .source(project.getId())
                    .args(upn, project.getName())
                    .build()));
        }
    }

    @HandleBeforeSave
    public void preHandleProjectSave(Project current) {
        Assert.isTrue(RunningStatus.STOP != current.getStatus(),"Project can not be modify after STOP");
    }

    @HandleAfterSave
    public void postHandleProjectSave(Project current) {
        if(RunningStatus.STOP == current.getStatus()) {
            // Don't check if status is stop
            return;
        }
        Optional<Project> previousVersion = auditService.getPreviousVersion(current);
        previousVersion.ifPresent(previous -> {
            if(log.isDebugEnabled()) {
                log.debug("Previous: {}, current: {}", previous, current);
            }
            // if 'name' is changed
            if(!Objects.equals(previous.getName(), current.getName())) {
                eventService.emit(Event.builder()
                        .type(EventType.PROJECT_PROPERTIES_NAME_CHANGE)
                        .source(current.getId())
                        .args(previous.getName(), current.getName())
                        .build());
            }
            // if 'manager' is changed
            if(!Objects.equals(previous.getManager(), current.getManager())) {
                eventService.emit(Event.builder()
                        .type(EventType.PROJECT_MANAGER_CHANGED)
                        .source(current.getId())
                        .args(current.getName(), current.getManager())
                        .build());
            }
            // if 'owner' is changed
            if(!Objects.equals(previous.getOwner(), current.getOwner())) {
                eventService.emit(Event.builder()
                        .type(EventType.PROJECT_OWNER_CHANGED)
                        .source(current.getId())
                        .args(current.getName(), current.getOwner())
                        .build());
            }
            // if 'member' was removed
            Set<String> previousMembers = new HashSet<>(previous.getMembers());
            previousMembers.removeAll(new HashSet<>(current.getMembers()));
            previousMembers.forEach(upn -> eventService.emit(Event.builder()
                    .type(EventType.PROJECT_MEMBER_REMOVED)
                    .source(previous.getId())
                    .args(upn, previous.getName())
                    .build()));

            // if 'member' was added
            Set<String> currentMembers = new HashSet<>(current.getMembers());
            currentMembers.removeAll(new HashSet<>(previous.getMembers()));
            currentMembers.forEach(upn -> eventService.emit(Event.builder()
                    .type(EventType.PROJECT_MEMBER_ADDED)
                    .source(current.getId())
                    .args(upn, current.getName())
                    .build()));

        });
    }

    @HandleBeforeDelete
    public void preHandleProjectDelete(Project current) {
        Assert.isTrue(RunningStatus.STOP == current.getStatus() && current.isCancelled(),
                "Only Cancelled Project can be deleted");
    }

    @HandleAfterDelete
    public void postHandleProjectDelete(Project tobeRemoved) {
        eventService.emit(Event.builder()
                .type(EventType.EVENT_PROJECT_REMOVED)
                .source(tobeRemoved.getId())
                .args(tobeRemoved.getName())
                .build());
    }


    public void cancelProject(long id) {
        UserContext userContext = userContextHolder.getUserContext();
        String currentUpn = userContext.getUpn();
        Project project = projectRepository.getOne(id);

        // Verify owner and status
        Assert.isTrue(Objects.equals(project.getOwner(), currentUpn), "Only project owner can cancel the project");
        Assert.isTrue(!Objects.equals(project.getStatus(), RunningStatus.STOP), "Project can be canceled at most once");

        // Do action
        project.setStatus(RunningStatus.STOP);
        project.setCancelled(true);
        projectRepository.save(project);

        // Send event to all participants
        eventService.emit(Event.builder()
                .type(EventType.PROJECT_CANCELED)
                .source(project.getId())
                .args(project.getName())
                .build());
    }
}
