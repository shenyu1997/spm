package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tech.kuiperbelt.spm.domain.core.support.AuditService;
import tech.kuiperbelt.spm.domain.core.support.UserContext;
import tech.kuiperbelt.spm.domain.core.support.UserContextHolder;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.event.EventService;
import tech.kuiperbelt.spm.domain.core.event.PropertiesChanged;
import tech.kuiperbelt.spm.domain.core.event.PropertyChanged;

import java.util.*;

import static tech.kuiperbelt.spm.domain.core.event.Event.*;

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

    @Autowired
    private WorkItemService workItemService;

    @Autowired
    private NoteService noteService;

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
        sendEvent(PROJECT_CREATED, project);

        if(!StringUtils.isEmpty(project.getOwner())) {
            sendEvent(PROJECT_OWNER_CHANGED, project, PropertyChanged.builder()
                    .property(Project.Fields.owner)
                    .newValue(project.getOwner())
                    .build());
        }

        // set manager if need
        if(!StringUtils.isEmpty(project.getManager())) {
            sendEvent(PROJECT_MANAGER_CHANGED, project, PropertyChanged.builder()
                    .property(Project.Fields.manager)
                    .newValue(project.getManager())
                    .build());
        }

        // set member changed
        if(!CollectionUtils.isEmpty(project.getMembers())) {
            String membersUpn = String.join(", ", project.getMembers());
            sendMemberAddEvent(project, membersUpn, new HashSet<>(project.getMembers()));
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
            PropertiesChanged.of(previous, current, Project.Fields.name).ifPresent(propertiesChanged ->
                    sendEvent(PROJECT_PROPERTIES_CHANGED, current, propertiesChanged));

            // if 'manager' is changed
            PropertyChanged.of(previous, current, Project.Fields.manager).ifPresent(propertyChanged ->
                    sendEvent(PROJECT_MANAGER_CHANGED, current, propertyChanged));

            // if 'owner' is changed
            PropertyChanged.of(previous, current, Project.Fields.owner).ifPresent(propertyChanged ->
                    sendEvent(PROJECT_OWNER_CHANGED, current, propertyChanged));

            // if 'member' was removed
            Set<String> previousMembers = new HashSet<>(previous.getMembers());
            previousMembers.removeAll(new HashSet<>(current.getMembers()));
            String previousMembersUpn = String.join(", ", previousMembers);
            sendMemberDeleteEvent(previous, previousMembers, previousMembersUpn);

            // if 'member' was added
            Set<String> currentMembers = new HashSet<>(current.getMembers());
            currentMembers.removeAll(new HashSet<>(previous.getMembers()));
            String currentMembersUpn = String.join(", ", currentMembers);
            sendMemberAddEvent(current, currentMembersUpn, currentMembers);
        });
    }



    @HandleBeforeDelete
    public void preHandleProjectDelete(Project current) {
        Assert.isTrue(current.isCanBeDeleted(),
                "Only Cancelled Project can be deleted");

        noteService.deleteNoteByParent(current.getId());

        current.getPhases()
                .stream()
                .sorted(Comparator.comparingInt(Phase::getSeq).reversed())
                .forEach(phase ->
                phaseService.deletePhase(phase));

        current.getDirectWorkItems()
                .forEach(workItem ->
                        workItemService.deleteWorkItem(workItem));
    }

    @HandleAfterDelete
    public void postHandleProjectDelete(Project tobeRemoved) {
        sendEvent(PROJECT_DELETED, tobeRemoved);
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

        project.getDirectWorkItems().stream()
                .filter(WorkItem::isCanBeCancelled)
                .forEach(workItem ->
                        workItemService.cancelWorkItem(workItem.getId()));

        // Do action
        project.cancel();
        projectRepository.save(project);

        // Send event to all participants
        sendEvent(PROJECT_CANCELED, project);
    }

    public Project getProjectById(Long projectId) {
        return projectRepository.getOne(projectId);
    }

    public void startProject(long id) {
        Project project = projectRepository.getOne(id);
        project.start();
        sendEvent(PROJECT_STARTED, project);
        project.getPhases().stream().findFirst().ifPresent(phase ->
                phaseService.startPhase(phase));
    }

    public void doneProject(long id) {
        Project project = projectRepository.getOne(id);
        Assert.isTrue(project.isCanBeDone(), "Project can not be done yet.");

        project.done();
        sendEvent(PROJECT_DONE, project);
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

    public WorkItem createDirectWorkItem(Long id, WorkItem workItem) {
        Project project = projectRepository.getOne(id);
        workItem.setProject(project);
        WorkItem createdWorkItem = workItemService.createWorkItemInContext(workItem);
        project.setAllDirItemsStop(false);
        return createdWorkItem;
    }

    public List<WorkItem> getDirectWorkItems(Long id) {
        return projectRepository.getOne(id)
                .getDirectWorkItems();
    }

    @Async
    @EventListener(condition = "#root.args[0].key == '" + ITEM_PROJECT_CHANGED + "'")
    public void handleWorkItemMovedEvent(Event event) {
        userContextHolder.runAs(event.getUserContext(), () -> {
            PropertyChanged propertyChanged = PropertyChanged.of((Map<Object, Object>)event.getArgs()[1]);
            // We only need check old phase's allItemsStop because new phase has already done
            propertyChanged.getOldValue().ifPresent(oldId ->
                    projectRepository.getOne(Long.valueOf((String)oldId)).checkAllDirItemsStop());
        });
    }

    private void sendEvent(String key, Project project) {
        sendEvent(key, project, PropertiesChanged.builder().build());
    }

    private void sendEvent(String key, Project project, PropertyChanged propertyChanged) {
        sendEvent(key, project, PropertiesChanged.ofSingle(propertyChanged));
    }

    private void sendEvent(String key, Project project, PropertiesChanged propertiesChanged) {
        Event.EventBuilder builder = Event.builder()
                .key(key)
                .source(project);

        switch (key) {
            case Event.PROJECT_CREATED:
            case Event.PROJECT_DELETED:
            case Event.PROJECT_CANCELED:
            case Event.PROJECT_STARTED:
            case Event.PROJECT_DONE:
            case Event.PROJECT_NOTE_TAKEN:
                builder.args(project.getName());
                break;
            case Event.PROJECT_OWNER_CHANGED:
                builder.args(project.getName(),
                        propertiesChanged.getPropertyChanged(Project.Fields.owner));
                break;
            case Event.PROJECT_MANAGER_CHANGED:
                builder.args(project.getName(),
                        propertiesChanged.getPropertyChanged(Project.Fields.manager));
                break;
            case Event.PROJECT_PROPERTIES_CHANGED:
                builder.args(project.getName(),propertiesChanged);
                break;

            default:
                throw new IllegalArgumentException("Unsupported event key:" + key);
        }
        eventService.emit(builder.build());
    }

    private void sendMemberAddEvent(Project project, String membersUpn, Set<String> currentMembers) {
        if(currentMembers.isEmpty()) {
            return;
        }
        eventService.emit(Event.builder()
                .key(PROJECT_MEMBER_ADDED)
                .source(project.getId())
                .args(membersUpn, project.getName(), currentMembers)
                .build());
    }

    private void sendMemberDeleteEvent(Project previous, Set<String> previousMembers, String previousMembersUpn) {
        if(previousMembers.isEmpty()) {
            return;
        }
        eventService.emit(Event.builder()
                .key(PROJECT_MEMBER_DELETED)
                .source(previous.getId())
                .args(previousMembersUpn, previous.getName(), previousMembers)
                .build());
    }

    public Note takeNote(long projectId, Note note) {
        Project project = projectRepository.getOne(projectId);
        note.setParent(project);
        Note createdNote = noteService.takeNote(note);
        sendEvent(PROJECT_NOTE_TAKEN, project);
        return createdNote;
    }

    public List<Note> getNotes(Long id) {
        return noteService.findByParent(id);
    }
}
