package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.event.EventService;
import tech.kuiperbelt.spm.domain.core.event.PropertiesChanged;
import tech.kuiperbelt.spm.domain.core.event.PropertyChanged;
import tech.kuiperbelt.spm.domain.core.support.AuditService;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;
import tech.kuiperbelt.spm.domain.core.support.UserContextHolder;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Transactional
@Service
@RepositoryEventHandler
public class WorkItemService {
    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserContextHolder userContextHolder;

    @Autowired
    private NoteService noteService;

    /**
     * Context means workItem belong to Project or Phase
     * @param workItem
     * @return
     */
    public WorkItem createWorkItemInContext(WorkItem workItem) {
        preHandleCreate(workItem);
        WorkItem savedWorkItem = workItemRepository.save(workItem);
        postHandleCreate(workItem);
        return savedWorkItem;
    }

    @HandleBeforeCreate
    public void preHandleCreate(WorkItem workItem) {

        // Set owner
        String currentUpn = userContextHolder.getUserContext().getUpn();
        workItem.setOwner(currentUpn);

        // Set default assignee if workItem has no phase
        if(workItem.getPhase() == null && workItem.getProject() ==null && StringUtils.isEmpty(workItem.getAssignee())) {
            workItem.setAssignee(currentUpn);
        }

        // Determine the scope
        workItem.determineScope();
    }


    @HandleAfterCreate
    public void postHandleCreate(WorkItem workItem) {
        // send workItem created event
        if(workItem.getPhase() != null) {
            sendEvent(Event.PHASE_ITEM_ADDED, workItem);
        } else if(workItem.getProject() != null) {
            sendEvent(Event.PROJECT_ITEM_ADDED, workItem);
        } else {
            sendEvent(Event.ITEM_ADDED, workItem);
        }
        // send assignee event
        if(!StringUtils.isEmpty(workItem.getAssignee())) {
            sendEvent(Event.ITEM_ASSIGNEE_CHANGED, workItem);
        }

        // send is ready event (assignee not empty)
        if(workItem.getReady() != null && workItem.getReady()) {
            sentReadyEventIfWorkItemReady(workItem);
        }
    }

    @HandleBeforeSave
    public void preHandleSave(WorkItem workItem) {
        Assert.isTrue(workItem.getStatus() != RunningStatus.STOP, "STOP work item can not be updated");
        // Determine the scope
        workItem.determineScope();
    }

    @HandleAfterSave
    public void postHandleSave(WorkItem workItem) {
        // Check move phase
        auditService.getPreviousVersion(workItem)
                .ifPresent(previous -> {
                    PropertyChanged.of(previous, workItem, WorkItem.Fields.phase)
                            .ifPresent(propertyChanged ->
                                    movePhase(workItem, propertyChanged));
                    PropertyChanged.of(previous, workItem, WorkItem.Fields.project)
                            .ifPresent(propertyChanged ->
                                    moveProject(workItem, propertyChanged));
                });

        // check overflow
        if(workItem.isOverflow()) {
            sendEvent(Event.ITEM_OVERFLOW_TRUE, workItem);
        }

        Optional<WorkItem> previousVersion = auditService.getPreviousVersion(workItem);
        previousVersion.ifPresent(previousItem -> {
            PropertyChanged.of(previousItem, workItem, WorkItem.Fields.owner).ifPresent(propertyChanged ->
                    sendEvent(Event.ITEM_OWNER_CHANGED,workItem, propertyChanged));
            PropertyChanged.of(previousItem, workItem, WorkItem.Fields.owner).ifPresent(propertyChanged ->
                    sendEvent(Event.ITEM_ASSIGNEE_CHANGED, workItem, propertyChanged));

            PropertiesChanged.of(previousItem, workItem,
                    WorkItem.Fields.priority,
                    WorkItem.Fields.milestone,
                    WorkItem.Fields.name)
                    .ifPresent(propertiesChanged ->
                            sendEvent(Event.ITEM_PROPERTIES_CHANGED, workItem, propertiesChanged));
        });

    }

    @HandleBeforeLinkDelete
    public void preHandleLinkDelete(WorkItem workItem, BaseEntity phaseOrProject) {
        if(phaseOrProject instanceof Phase) {
            workItem.setScope(WorkItem.Scope.PROJECT);
        } else if(phaseOrProject instanceof Project) {
            Assert.isTrue(workItem.getScope() == WorkItem.Scope.PROJECT ,"WorkItem must be PROJECT scope");
            workItem.setScope(WorkItem.Scope.PERSON);
        }
    }

    @HandleAfterLinkDelete
    public void postHandleLinkDelete(WorkItem workItem, BaseEntity phaseOrProject) {
        postHandleSave(workItem);
    }

    public void startWorkItem(long workItemId) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        workItem.start();

        // send start event
        sendEvent(Event.ITEM_STARTED, workItem);
    }

    public void doneWorkItem(long workItemId) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        workItem.done();

        // send done event;
        sendEvent(Event.ITEM_DONE, workItem);

        checkRelatedCanBeDone(workItem);
    }

    private void checkRelatedCanBeDone(WorkItem workItem) {
        if(workItem.getPhase() != null) {
            workItem.getPhase().checkAllItemsStop();
        } else if(workItem.getProject() != null) {
            workItem.getProject().checkAllDirItemsStop();
        }
    }

    public void setWorkItemsReady(Phase phase) {
        phase.getWorkItems()
                .forEach(this::sentReadyEventIfWorkItemReady);
    }

    private void sentReadyEventIfWorkItemReady(WorkItem workItem) {
        if(workItem.getReady() && workItem.getAssignee() != null) {
            sendEvent(Event.ITEM_READY_TRUE, workItem);
        }
    }


    public void cancelWorkItems(Phase phase) {
        phase.getWorkItems()
                .stream()
                .filter(WorkItem::isCanBeCancelled)
                .forEach(this::cancelWorkItem);
    }

    public void cancelWorkItem(Long workItemId) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        cancelWorkItem(workItem);
    }

    private void cancelWorkItem(WorkItem workItem) {
        workItem.cancel();
        sendEvent(Event.ITEM_CANCELED, workItem);

        checkRelatedCanBeDone(workItem);
    }

    public void deleteWorkItems(Phase phase) {
        List<WorkItem> workItems = new ArrayList<>(phase.getWorkItems());
        workItems.forEach(this::deleteWorkItem);
    }

    public void deleteWorkItem(WorkItem workItem) {
        preHandleDelete(workItem);
        workItemRepository.delete(workItem);
        postHandleDelete(workItem);
    }

    @HandleBeforeDelete
    public void preHandleDelete(WorkItem workItem) {
        Assert.isTrue(workItem.isCanBeDeleted(), "WorkItem can not be delete");
        workItem.getNotes()
                .forEach(noteService::deleteNote);
    }

    @HandleAfterDelete
    public void postHandleDelete(WorkItem workItem) {
        if(workItem.getPhase() != null) {
            workItem.getPhase().checkAllItemsStopAfterRemove(workItem);
        } else if(workItem.getProject() != null) {
            workItem.getProject().checkAllDirItemsStopAfterRemove(workItem);
        }
        sendEvent(Event.ITEM_DELETED, workItem);
    }

    public void moveWorkItems(Phase phase, Period offSet) {
        phase.getWorkItems()
                .forEach(item -> this.moveWorkItem(item, offSet));
    }

    public List<WorkItem> findByPhase(Phase phase) {
        return workItemRepository.findByPhase(phase);
    }

    private void moveWorkItem(WorkItem workItem, Period offset) {
        if(workItem.move(offset)) {
            sendMoveEvent(workItem, offset);
        }
    }

    private void movePhase(WorkItem workItem, PropertyChanged propertyChanged) {
        propertyChanged.getOldValue().ifPresent(oldValue ->
                Assert.isTrue(((Phase)oldValue).getStatus() != RunningStatus.STOP,
                "STOP phase can not move workItem out"));

        propertyChanged.getNewValue().ifPresent(newVale -> {
            Phase newPhase = (Phase) newVale;
            Assert.isTrue(newPhase.getStatus() != RunningStatus.STOP,
                    "STOP phase can not move workItem in");
            // Only set new phase'allItemsStop to true because we add Non STOP workItem to it;
            // and We postpone checking of old item to async procession because the DB need update and commit first.
            if(workItem.getStatus() != RunningStatus.STOP) {
                newPhase.setAllItemStop(false);
            }
        });

        sentReadyEventIfWorkItemReady(workItem);
        sendEvent(Event.ITEM_PHASE_CHANGED, workItem, propertyChanged.map(Phase.class, Phase::getId));
    }
    private void moveProject(WorkItem workItem, PropertyChanged propertyChanged) {
        propertyChanged.getOldValue().ifPresent(oldValue ->
                Assert.isTrue(((Project)oldValue).getStatus() != RunningStatus.STOP,
                        "STOP project can not move workItem out"));

        propertyChanged.getNewValue().ifPresent(newVale -> {
            Project newProject = (Project) newVale;
            Assert.isTrue(newProject.getStatus() != RunningStatus.STOP,
                    "STOP project can not move workItem in");
            // Only set new project'allDirItemsStop to true because we add Non STOP workItem to it;
            // and We postpone checking of old item to async procession because the DB need update and commit first.
            if(workItem.getStatus() != RunningStatus.STOP && workItem.getScope() == WorkItem.Scope.PROJECT) {
                newProject.setAllDirItemsStop(false);
            }
        });

        sendEvent(Event.ITEM_PROJECT_CHANGED, workItem, propertyChanged.map(Project.class, Project::getId));
    }

    private void sendEvent(String key, WorkItem workItem) {
        sendEvent(key, workItem, PropertiesChanged.builder().build());
    }

    private void sendEvent(String key, WorkItem workItem, PropertyChanged propertyChanged) {
        sendEvent(key, workItem, PropertiesChanged.ofSingle(propertyChanged));
    }

    private void sendEvent(String key, WorkItem workItem, PropertiesChanged propertiesChanged) {
        Event.EventBuilder builder = Event.builder()
                .key(key)
                .source(workItem);

        switch (key) {
            case Event.ITEM_ADDED:
                builder.args(workItem.getName(),
                        Optional.of(workItem).map(WorkItem::getPhase).map(Phase::getName).orElse(""),
                        Optional.of(workItem).map(WorkItem::getProject).map(Project::getName).orElse(""));
                break;
            case Event.PROJECT_ITEM_ADDED:
            case Event.PHASE_ITEM_ADDED:
            case Event.ITEM_DELETED:
            case Event.ITEM_STARTED:
            case Event.ITEM_DONE:
            case Event.ITEM_CANCELED:
            case Event.ITEM_READY_TRUE:
            case Event.ITEM_OVERFLOW_TRUE:
                builder.args(workItem.getName());
                break;
            case Event.ITEM_OWNER_CHANGED:
                builder.args(workItem.getName(), workItem.getOwner());
                break;
            case Event.ITEM_ASSIGNEE_CHANGED:
                builder.args(workItem.getName(), workItem.getAssignee());
                break;

            case Event.ITEM_PROPERTIES_CHANGED:
                builder.args(workItem.getName(), propertiesChanged);
                break;
            case Event.ITEM_START_CHANGED:
                builder.args(workItem.getName(), propertiesChanged.getPropertyChanged(WorkItem.Fields.plannedStartDate));
                break;
            case Event.ITEM_END_CHANGED:
                builder.args(workItem.getName(), propertiesChanged.getPropertyChanged(WorkItem.Fields.deadLine));
                break;
            case Event.ITEM_PHASE_CHANGED:
                builder.args(workItem.getName(), propertiesChanged.getPropertyChanged(WorkItem.Fields.phase));
                break;
            case Event.ITEM_PROJECT_CHANGED:
                builder.args(workItem.getName(), propertiesChanged.getPropertyChanged(WorkItem.Fields.project));
                break;
            default:
                throw new IllegalArgumentException("Unsupported event key:" + key);
        }
        eventService.emit(builder.build());
    }

    public void sendMoveEvent(WorkItem workItem, Period offset) {
        Assert.notNull(workItem, "WorkItem can not be null");
        Event.EventBuilder builder = Event.builder().source(workItem);
        if(offset.isNegative()) {
            builder.key(Event.ITEM_MOVED_LEFT);
        } else {
            builder.key(Event.ITEM_MOVED_RIGHT);
        }
        eventService.emit(builder.args(workItem.getName(), offset.getDays()).build());
    }

    public Note takeNote(long workItemId, Note note) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        Assert.notNull(workItem, "WorkItem can not be null");
        note.setWorkItem(workItem);
        return noteService.takeNote(note);
    }

}
