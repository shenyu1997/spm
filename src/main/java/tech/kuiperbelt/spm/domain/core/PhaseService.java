package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.core.support.AuditService;
import tech.kuiperbelt.spm.domain.core.support.UserContextHolder;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.event.EventService;
import tech.kuiperbelt.spm.domain.core.event.PropertiesChanged;
import tech.kuiperbelt.spm.domain.core.event.PropertyChanged;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static tech.kuiperbelt.spm.domain.core.event.Event.*;

@Transactional
@Setter
@Service
@RepositoryEventHandler
public class PhaseService {

    public static final int FIRST = 0;
    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private WorkItemService workItemService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserContextHolder userContextHolder;

    /**
     * It will be trigger by project delete so just delete workItems cascade
     */
    public void deletePhase(Phase phase) {
        preHandlePhaseDelete(phase);
        phaseRepository.delete(phase);
        postHandlePhaseDelete(phase);
    }

    @HandleBeforeDelete
    public void preHandlePhaseDelete(Phase phase) {
        // then remove
        Assert.isTrue(phase.isCanBeDeleted(), "Running phase can not be deleted");
        deleteWorkItems(phase);
        List<Phase> allPhases = phase.getProject().getPhases();
        allPhases.remove(phase);
        List<Phase> laterImpactedPhases = new LinkedList<>(allPhases.subList(phase.getSeq(), allPhases.size()));
        resetSeq(allPhases);
        movePhases(phase.getPeriod().negated(), laterImpactedPhases);

        // check project is allPhasesStop
        phase.getProject().checkAllPhaseStop();
    }

    @HandleAfterDelete
    public void postHandlePhaseDelete(Phase phase) {
        sendEvent(Event.PROJECT_SCHEDULE_PHASE_REMOVED, phase);
    }

    public Phase appendPhase(Project project, Phase phase) {
        phase.setSeq(project.getPhases().size());
        return insertPhase(project, phase);
    }

    public Phase insertPhase(Project project, Phase phase) {
        Assert.isTrue(project.getStatus() != RunningStatus.STOP, "STOP project can not insert phase");

        List<Phase> allPhases = project.getPhases();

        // Check seq in reasonable range
        Assert.notNull(phase.getSeq(), "Seq can not be null when insert a phase");
        Assert.isTrue(phase.getSeq() >= 0 && phase.getSeq() <= allPhases.size(),
                "Seq should be in range 0 to " + allPhases.size());

        Optional<Phase> firstRunningPhase = allPhases.stream()
                .filter(p -> p.getStatus() == RunningStatus.RUNNING).findFirst();

        firstRunningPhase.ifPresent(value -> Assert.isTrue(value.getSeq() < phase.getSeq(),
                "Only can insert before an INIT phase"));

        phase.setProject(project);
        phase.setStatus(RunningStatus.INIT);

        if(phase.getSeq() != FIRST) {
            phase.setPlannedStartDate(allPhases.get(phase.getSeq() - 1)
                    .getPlannedEndDate().plusDays(1));
        }
        validateBeforeSave(phase);
        List<Phase> laterImpactedPhases = new LinkedList<>(allPhases.subList(phase.getSeq(), allPhases.size()));
        allPhases.add(phase.getSeq(), phase);
        resetSeq(allPhases);
        if(!laterImpactedPhases.isEmpty()) {
            Period offset = phase.getSeq() == FIRST?
                    Period.between(laterImpactedPhases.get(FIRST).getPlannedStartDate(), phase.getPlannedEndDate().plusDays(1)):
                    phase.getPeriod().plusDays(1);
            movePhases(offset, laterImpactedPhases);
        }

        Phase createPhase = phaseRepository.save(phase);
        sendEvent(PROJECT_SCHEDULE_PHASE_ADDED, createPhase);

        // start phase if Project is start and there is running phase
        if(project.getStatus() == RunningStatus.RUNNING && !firstRunningPhase.isPresent()) {
            startPhase(createPhase);
        }
        return createPhase;
    }

    @HandleBeforeSave
    public void preHandlePhaseSave(Phase phase) {
        Assert.isTrue(phase.getStatus() != RunningStatus.STOP, "Stopped phase can not be update");
        Phase previousVersion = auditService.getPreviousVersion(phase)
                .orElseThrow(() -> new IllegalStateException("Previous version of phase does not exist."));
        Assert.isTrue(!PropertyChanged.isChange(previousVersion, phase, Phase.Fields.seq),
                "Seq can not be change.");

        validateBeforeSave(phase);
        // Fetch workItem manually, because phase not load workItems cascade by spring data rest
        phase.setWorkItems(workItemService.findByPhase(phase));

        List<Phase> allPhases = phase.getProject().getPhases();

        // Change plannedStartDate
        if(PropertyChanged.isChange(previousVersion, phase, Phase.Fields.plannedStartDate)) {
            if(phase.getSeq() != FIRST) {
                // Change plannedStartDate will impact previous phase,
                // this change will also update previous phase's plannedEndDate
                Phase previousPhase = allPhases.get(phase.getSeq() - 1);
                LocalDate previousPlannedStartDate = previousPhase.getPlannedStartDate();
                Assert.isTrue(phase.getPlannedStartDate()
                        .isAfter(previousPlannedStartDate.plusDays(1)), // after planned end day
                        "The previous phase must have at least one day, the planned start date is too early");

                previousPhase.setPlannedEndDate(phase.getPlannedStartDate().minusDays(1));
                validateBeforeSave(previousPhase);
                sendEvent(PROJECT_SCHEDULE_PHASE_START_CHANGED, previousPhase);
            }

            // Change planned start date also need update time frame of it's workItems
            Period offset = Period.between(previousVersion.getPlannedStartDate(), phase.getPlannedStartDate());
            moveWorkItemsInPhase(phase, offset);
        }

        // Change planned end date will impact all phases after it.
        if(PropertyChanged.isChange(previousVersion, phase, Phase.Fields.plannedEndDate)) {
            if(phase.getSeq() < allPhases.size() - 1) {
                Period offset = Period.between(previousVersion.getPlannedEndDate(), phase.getPlannedEndDate());
                List<Phase> laterImpactedPhases = new LinkedList<>(allPhases.subList(phase.getSeq() + 1, allPhases.size()));
                movePhases(offset, laterImpactedPhases);
            }
        }
    }

    @HandleAfterSave
    public void postHandlePhaseSave(Phase phase) {
        Phase previousVersion = auditService.getPreviousVersion(phase)
                .orElseThrow(() -> new IllegalStateException("Previous version of phase does not exist."));

        PropertiesChanged.of(previousVersion, phase, Phase.Fields.name).ifPresent(propertiesChanged ->
                sendEvent(PROJECT_PHASE_PROPERTIES_CHANGE, phase, propertiesChanged));

        PropertyChanged.of(previousVersion, phase, Phase.Fields.plannedStartDate).ifPresent(propertyChanged ->
                sendEvent(PROJECT_SCHEDULE_PHASE_START_CHANGED, phase, propertyChanged));

        PropertyChanged.of(previousVersion, phase, Phase.Fields.plannedEndDate).ifPresent(propertyChanged ->
                sendEvent(PROJECT_SCHEDULE_PHASE_END_CHANGED, phase, propertyChanged));
    }

    private void resetSeq(List<Phase> phases) {
        if(CollectionUtils.isEmpty(phases)) {
            return;
        }

        // Reset planned start date, planned end data
        for(int i=1; i<phases.size(); i++) {
            Phase current = phases.get(i);
            current.setSeq(i);
        }
    }

    private void movePhases(Period offset, List<Phase> laterImpactedPhases) {
        if(offset.isZero() || CollectionUtils.isEmpty(laterImpactedPhases)) {
            return;
        }
        String eventKey = offset.isNegative()?
                Event.PROJECT_SCHEDULE_PHASE_MOVED_LEFT:
                Event.PROJECT_SCHEDULE_PHASE_MOVED_RIGHT;
        laterImpactedPhases.forEach( phase -> {
            phase.move(offset);
            validateBeforeSave(phase);
            moveWorkItemsInPhase(phase, offset);
            sendEvent(eventKey, phase, offset);
        });
    }

    private void validateBeforeSave(Phase phase) {
        Assert.notNull(phase.getPlannedStartDate(), "Planned start date of first phase can not be null.");
        Assert.isTrue(phase.getPlannedStartDate().isBefore(phase.getPlannedEndDate()),
                "Planned start date must before planned end date");
        Assert.notNull(phase.getProject(), "Phase.project can not be null");
    }

    public void startPhase(Phase phase) {
        phase.start();
        workItemService.setWorkItemsReady(phase);
        sendEvent(PROJECT_EXECUTION_PHASE_START, phase);
    }

    public void cancelPhase(long id) {
        Phase phase = phaseRepository.getOne(id);
        cancelWorkItems(phase);
        phase.cancel();
        Project project = phase.getProject();
        sendEvent(PROJECT_EXECUTION_PHASE_CANCEL, phase);
        // check project allPhaseStop
        project.checkAllPhaseStop();
    }

    public void donePhase(long id) {
        Phase phase = phaseRepository.getOne(id);
        Assert.isTrue(phase.isCanBeDone(), "Phase can not be done");
        phase.done();
        List<Phase> allPhases = phase.getProject().getPhases();

        if(phase.getSeq() < allPhases.size() - 1) {
            Phase nextPhase = allPhases.get(phase.getSeq() + 1);
            startPhase(nextPhase);
        } else {
            phase.getProject().checkAllPhaseStop();
        }
        sendEvent(PROJECT_EXECUTION_PHASE_DONE, phase);
    }

    public WorkItem createWorkItem(long phaseId, WorkItem workItem) {
        Phase phase = phaseRepository.getOne(phaseId);
        Assert.isTrue(phase.getStatus() != RunningStatus.STOP, "STOP phase can not add workItem.");
        WorkItem createdWorkItem = workItemService.createWorkItem(phase, workItem);

        // check can be done with project
        phase.setAllItemStop(false);
        return createdWorkItem;
    }

    @Async
    @EventListener(condition = "#root.args[0].key == '" + ITEM_SCHEDULE_MOVE_PHASE + "'")
    public void handleWorkItemMovedEvent(Event event) {
        userContextHolder.runAs(event.getUserContext(), () -> {
            PropertyChanged propertyChanged = PropertyChanged.of((Map<Object, Object>)event.getArgs()[1]);
            // We only need check old phase's allItemsStop because new phase has already done
            propertyChanged.getOldValue().ifPresent(oldId ->
                    phaseRepository.getOne(Long.valueOf((String)oldId)).checkAllItemsStop());
        });
    }

    private void cancelWorkItems(Phase phase) {
        workItemService.cancelWorkItems(phase);
    }

    private void deleteWorkItems(Phase phase) {
        workItemService.deleteWorkItems(phase);
    }

    /**
     * Move time frame of all workItems in phase
     * @param phase phase
     * @param offSet offSet
     */
    private void moveWorkItemsInPhase(Phase phase, Period offSet) {
        workItemService.moveWorkItems(phase, offSet);
    }



    private void sendEvent(String key, Phase phase) {
        sendEvent(key, phase, PropertiesChanged.builder().build());
    }

    private void sendEvent(String key, Phase phase, PropertyChanged propertyChanged) {
        sendEvent(key, phase, PropertiesChanged.ofSingle(propertyChanged));
    }

    private void sendEvent(String key, Phase phase, PropertiesChanged propertiesChanged) {
        Event.EventBuilder builder = Event.builder()
                .key(key)
                .source(phase);

        switch (key) {
            case Event.PROJECT_SCHEDULE_PHASE_REMOVED:
            case Event.PROJECT_SCHEDULE_PHASE_ADDED:
            case Event.PROJECT_EXECUTION_PHASE_CANCEL:
            case Event.PROJECT_EXECUTION_PHASE_DONE:
            case Event.PROJECT_EXECUTION_PHASE_START:
                builder.args(phase.getProject().getName(),
                        phase.getName());
                break;
            case Event.PROJECT_SCHEDULE_PHASE_START_CHANGED:
                builder.args(phase.getProject().getName(),
                        phase.getName(),
                        phase.toString(),
                        phase.getPlannedStartDate().toString());
                break;
            case Event.PROJECT_PHASE_PROPERTIES_CHANGE:
                builder.args(phase.getProject().getName(), phase.getName(),
                        propertiesChanged);
                break;
            case Event.ITEM_EXECUTION_DONE:
                builder.args(phase.getProject().getName(), phase.getName(),
                    propertiesChanged.getPropertyChanged(Phase.Fields.plannedStartDate));
                break;
            case Event.PROJECT_SCHEDULE_PHASE_END_CHANGED:
                builder.args(phase.getProject().getName(), phase.getName(),
                        propertiesChanged.getPropertyChanged(Phase.Fields.plannedEndDate));
                break;

            default:
                throw new IllegalArgumentException("Unsupported event key:" + key);
        }
        eventService.emit(builder.build());
    }

    private void sendEvent(String eventKey, Phase phase, Period offset) {
        eventService.emit(Event.builder()
                .key(eventKey)
                .source(phase.getId())
                .args(phase.getProject().getName(), phase.getName(), Math.abs(offset.getDays()))
                .build());
    }

}
