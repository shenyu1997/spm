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
import tech.kuiperbelt.spm.domain.event.PropertiesChanged;
import tech.kuiperbelt.spm.domain.event.PropertyChanged;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.kuiperbelt.spm.domain.event.Event.*;

@Setter
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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PhaseService phaseService;

    public Project createProject(Project project) {
        preHandleProjectCreate(project);
        Project savedProject = projectRepository.save(project);
        postHandleProjectCreate(savedProject);
        return savedProject;
    }

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
                .key(PROJECT_CREATED)
                .source(project.getId())
                .args(project.getName())
                .build());

        if(!StringUtils.isEmpty(project.getOwner())) {
            eventService.emit(Event.builder()
                    .key(PROJECT_OWNER_CHANGED)
                    .source(project.getId())
                    .args(project.getName(), project.getOwner())
                    .build());
        }

        // set manager if need
        if(!StringUtils.isEmpty(project.getManager())) {
            eventService.emit(Event.builder()
                    .key(PROJECT_MANAGER_CHANGED)
                    .source(project.getId())
                    .args(project.getName(), project.getManager())
                    .build());
        }

        if(!CollectionUtils.isEmpty(project.getMembers())) {
            String membersUpn = project.getMembers().stream().collect(Collectors.joining(", "));
            eventService.emit(Event.builder()
                    .key(PROJECT_MEMBER_ADDED)
                    .source(project.getId())
                    .args(membersUpn, project.getName(), project.getMembers())
                    .build());
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
            if(PropertyChanged.isChange(previous, current, Project.Fields.name)) {
                eventService.emit(Event.builder()
                        .key(PROJECT_PROPERTIES_CHANGE)
                        .source(current.getId())
                        .args(PropertiesChanged.builder()
                                .append(Project.Fields.name, previous.getName(), current.getName())
                                .build())
                        .build());
            }

            // if 'manager' is changed
            if(PropertyChanged.isChange(previous, current, Project.Fields.manager)) {
                eventService.emit(Event.builder()
                        .key(PROJECT_MANAGER_CHANGED)
                        .source(current.getId())
                        .args(current.getName(), current.getManager())
                        .build());
            }

            // if 'owner' is changed
            if(PropertyChanged.isChange(previous, current, Project.Fields.owner)) {
                eventService.emit(Event.builder()
                        .key(PROJECT_OWNER_CHANGED)
                        .source(current.getId())
                        .args(current.getName(), current.getOwner())
                        .build());
            }

            // if 'member' was removed
            Set<String> previousMembers = new HashSet<>(previous.getMembers());
            previousMembers.removeAll(new HashSet<>(current.getMembers()));
            String previousMembersUpn = previousMembers.stream().collect(Collectors.joining(", "));
            eventService.emit(Event.builder()
                    .key(PROJECT_MEMBER_REMOVED)
                    .source(previous.getId())
                    .args(previousMembersUpn, previous.getName(), previousMembers)
                    .build());

            // if 'member' was added
            Set<String> currentMembers = new HashSet<>(current.getMembers());
            currentMembers.removeAll(new HashSet<>(previous.getMembers()));
            String currentMembersUpn = currentMembers.stream().collect(Collectors.joining(", "));
            eventService.emit(Event.builder()
                    .key(PROJECT_MEMBER_ADDED)
                    .source(current.getId())
                    .args(currentMembersUpn, current.getName(), currentMembers)
                    .build());
        });
    }

    @HandleBeforeDelete
    public void preHandleProjectDelete(Project current) {
        Assert.isTrue(current.isCanBeDeleted(),
                "Only Cancelled Project can be deleted");
        current.getPhases().stream()
                .forEach(phase ->
                phaseService.deletePhase(phase));
    }

    @HandleAfterDelete
    public void postHandleProjectDelete(Project tobeRemoved) {
        eventService.emit(Event.builder()
                .key(PROJECT_REMOVED)
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

        project.getPhases().stream()
                .filter(Phase::isCanBeCancelled)
                .forEach(phase ->
                phaseService.cancelPhase(phase.getId()));

        // Do action
        project.cancel();
        projectRepository.save(project);

        // Send event to all participants
        eventService.emit(Event.builder()
                .key(PROJECT_EXECUTION_PROJECT_CANCELED)
                .source(project.getId())
                .args(project.getName())
                .build());
    }

    public Project getProjectById(Long projectId) {
        return projectRepository.getOne(projectId);
    }

    public void startProject(long id) {
        Project project = projectRepository.getOne(id);
        project.start();
        eventService.emit(Event.builder()
                .key(PROJECT_EXECUTION_PROJECT_START)
                .source(project.getId())
                .args(project.getName())
                .build());

        project.getPhases().stream().findFirst().ifPresent(phase -> {
            phaseService.startPhase(phase);
        });
    }

    public void doneProject(long id) {
        Project project = projectRepository.getOne(id);
        Assert.isTrue(project.isCanBeDone(), "Project can not be done yet.");

        project.done();
        eventService.emit(Event.builder()
                .key(PROJECT_EXECUTION_PROJECT_DONE)
                .source(id)
                .args(project.getName())
                .build());
    }

    public Phase appendPhase(long id, Phase phase) {
        Project project = projectRepository.getOne(id);
        Phase createdPhase = phaseService.appendPhase(project, phase);
        project.setAllPhasesStop(false);
        return createdPhase;
    }

    public Phase insertPhase(Long id, Phase phase) {
        Project project = projectRepository.getOne(id);
        Phase createdPhase = phaseService.insertPhase(project, phase);
        project.setAllPhasesStop(false);
        return createdPhase;
    }
}
