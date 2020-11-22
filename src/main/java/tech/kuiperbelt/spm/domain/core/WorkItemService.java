package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import tech.kuiperbelt.spm.common.AuditService;
import tech.kuiperbelt.spm.common.UserContextHolder;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;
import tech.kuiperbelt.spm.domain.event.PropertiesChanged;
import tech.kuiperbelt.spm.domain.event.PropertyChanged;

import java.time.Period;
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

    public WorkItem createWorkItem(Phase phase, WorkItem workItem) {
        workItem.setPhase(phase);
        workItem.setProject(phase.getProject());
        preHandleCreate(workItem);
        WorkItem savedWorkItem = workItemRepository.save(workItem);
        postHandleCreate(workItem);
        return savedWorkItem;
    }

    @HandleBeforeCreate
    public void preHandleCreate(WorkItem workItem) {
        if(workItem.getPhase() != null) {
            Assert.isTrue(workItem.getPhase().getStatus() != RunningStatus.STOP, "Phase can not be STOP");
        }

        // set owner
        String currentUpn = userContextHolder.getUserContext().getUpn();
        workItem.setOperator(currentUpn);
    }

    @HandleAfterCreate
    public void postHandleCreate(WorkItem workItem) {
        // send workItem created event
        if(workItem.getPhase() != null) {
            sendEvent(Event.DETACH_ITEM_ADDED, workItem);
        } else {
            sendEvent(Event.ITEM_ADDED, workItem);
        }
        // send assignee event
        if(!StringUtils.isEmpty(workItem.getAssignee())) {
            sendEvent(Event.ITEM_ASSIGNEE_CHANGED, workItem);
        }

        // send is ready event (assignee not empty)
        if(workItem.isReady()) {
            setWorkItemReady(workItem);
        }
    }

    @HandleBeforeSave
    public void preHandleSave(WorkItem workItem) {
        Assert.isTrue(workItem.getStatus() != RunningStatus.STOP, "STOP work item can not be updated");
        auditService.getPreviousVersion(workItem).ifPresent(previous -> {
            if(PropertyChanged.isChange(previous, workItem, WorkItem.Fields.phase)) {
                movePhase(workItem);
            }
        });
    }

    @HandleAfterSave
    public void postHandleSave(WorkItem workItem) {
        // check overflow
        if(workItem.isOverflow()) {
            sendEvent(Event.ITEM_SCHEDULE_OVERFLOW, workItem);
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
                            sendEvent(Event.ITEM_PROPERTIES_CHANGE, workItem, propertiesChanged));
        });

    }


    public void startWorkItem(long workItemId) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        workItem.start();

        // send start event
        sendEvent(Event.ITEM_EXECUTION_START, workItem);
    }

    public void doneWorkItem(long workItemId) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        workItem.done();

        // send done event;
        sendEvent(Event.ITEM_EXECUTION_DONE, workItem);

        if(workItem.getPhase() != null) {
            workItem.getPhase().checkAllPhaseStop();
        }
    }

    public void setWorkItemsReady(Phase phase) {
        phase.getWorkItems()
                .forEach(this::setWorkItemReady);
    }

    private void setWorkItemReady(WorkItem workItem) {
        workItem.setReady(true);
        if(workItem.getAssignee() != null) {
            sendEvent(Event.ITEM_SCHEDULE_IS_READY, workItem);
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
        sendEvent(Event.ITEM_EXECUTION_CANCEL, workItem);

        if(workItem.getPhase() != null) {
            workItem.getPhase().checkAllPhaseStop();
        }
    }

    public void deleteWorkItems(Phase phase) {
        phase.getWorkItems()
                .forEach(this::deleteWorkItem);
    }

    private void deleteWorkItem(WorkItem workItem) {
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
        if(workItem.getPhase() != null && workItem.getPhase().getStatus() == RunningStatus.RUNNING) {
            workItem.getPhase().getWorkItems().remove(workItem);
            workItem.getPhase().checkAllPhaseStop();
        }
        sendEvent(Event.ITEM_REMOVE, workItem);
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

    private void movePhase(WorkItem workItem) {
        Assert.notNull(workItem.getPhase(), "Phase can not be null");
        Assert.isTrue(workItem.getPhase().getStatus() != RunningStatus.STOP,
                "STOP phase can not move workItem in");
        workItem.setProject(workItem.getPhase().getProject());
        boolean already = workItem.isReady();
        if(workItem.getPhase().getStatus() == RunningStatus.INIT) {
            workItem.setReady(false);
        } else {
            workItem.setReady(true);
            if(!already) { //means if ready changed
                setWorkItemReady(workItem);
            }
        }
        sendEvent(Event.ITEM_SCHEDULE_MOVE_PHASE, workItem);
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
                builder.args(workItem.getName(),workItem.getPhase().getName(), workItem.getProject().getName());
                break;
            case Event.DETACH_ITEM_ADDED:
            case Event.ITEM_REMOVE:
            case Event.ITEM_EXECUTION_START:
            case Event.ITEM_EXECUTION_DONE:
            case Event.ITEM_EXECUTION_CANCEL:
            case Event.ITEM_SCHEDULE_IS_READY:
            case Event.ITEM_SCHEDULE_OVERFLOW:
                builder.args(workItem.getName());
                break;
            case Event.ITEM_OWNER_CHANGED:
                builder.args(workItem.getName(), workItem.getOwner());
                break;
            case Event.ITEM_ASSIGNEE_CHANGED:
                builder.args(workItem.getName(), workItem.getAssignee());
                break;

            case Event.ITEM_PROPERTIES_CHANGE:
                builder.args(workItem.getName(), propertiesChanged);
                break;
            case Event.ITEM_SCHEDULE_START_CHANGED:
                builder.args(workItem.getName(), propertiesChanged.getPropertyChanged(WorkItem.Fields.plannedStartDate));
                break;
            case Event.ITEM_SCHEDULE_END_CHANGED:
                builder.args(workItem.getName(), propertiesChanged.getPropertyChanged(WorkItem.Fields.deadLine));
            case Event.ITEM_SCHEDULE_MOVE_PHASE:
                builder.args(workItem.getName(), workItem.getPhase().getName());
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
            builder.key(Event.ITEM_SCHEDULE_MOVE_LEFT);
        } else {
            builder.key(Event.ITEM_SCHEDULE_MOVE_RIGHT);
        }
        eventService.emit(builder.args(workItem.getName(), offset.getDays()).build());
    }

    public Note takeNote(long workItemId, Note note) {
        WorkItem workItem = workItemRepository.getOne(workItemId);
        return noteService.takeNote(workItem, note);
    }
}
