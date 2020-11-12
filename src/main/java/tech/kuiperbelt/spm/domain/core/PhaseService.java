package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.common.AuditService;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;
import tech.kuiperbelt.spm.domain.event.PropertiesChanged;
import tech.kuiperbelt.spm.domain.event.PropertyChanged;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedList;
import java.util.List;

import static tech.kuiperbelt.spm.domain.event.Event.*;

@Transactional
@Setter
@Service
@RepositoryEventHandler
public class PhaseService {

    public static final int FIRST = 0;
    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    /**
     * It will be trigger by project delete so just delete workItems cascade
     */
    public void deletePhase(Phase phase) {
        deleteWorkItems(phase);
        phaseRepository.delete(phase);
        postHandlePhaseDelete(phase);
    }

    @HandleBeforeDelete
    public void preHandlePhaseDelete(Phase phase) {
        Assert.isTrue(phase.getStatus() == RunningStatus.INIT, "Only INIT phase can be removed");

        deleteWorkItems(phase);
        List<Phase> allPhases = phase.getProject().getPhases();
        allPhases.remove(phase);
        List<Phase> laterImpactedPhases = new LinkedList<>(allPhases.subList(phase.getSeq(), allPhases.size()));
        resetSeq(allPhases);
        movePhases(phase.getPeriod().negated(), laterImpactedPhases);
    }

    @HandleAfterDelete
    public void postHandlePhaseDelete(Phase phase) {
        eventService.emit(Event.builder()
                .key(Event.PROJECT_SCHEDULE_PHASE_REMOVED)
                .source(phase.getId())
                .args(phase.getProject().getName(),
                        phase.getName(),
                        phase.getPlannedStartDate().toString(),
                        phase.getPlannedEndDate().toString()
                )
                .build());
    }

    public Phase appendPhase(Long projectId, Phase phase) {
        Project project = projectService.getProjectById(projectId);
        phase.setSeq(project.getPhases().size());
        return insertPhase(projectId, phase);
    }

    public Phase insertPhase(Long projectId, Phase phase) {
        Project project = projectService.getProjectById(projectId);
        Assert.isTrue(project.getStatus() != RunningStatus.STOP, "STOP project can not insert phase");

        phase.setProject(project);
        phase.setStatus(RunningStatus.INIT);

        // Check seq in reasonable range
        Assert.notNull(phase.getSeq(), "Seq can not be null when insert a phase");
        List<Phase> allPhases = project.getPhases();
        Assert.isTrue(phase.getSeq() >= 0 && phase.getSeq() <= allPhases.size(),
                "Seq should be in range 0 to " + allPhases.size());

        if(phase.getSeq() < allPhases.size()) {
            Assert.isTrue(allPhases.get(phase.getSeq()).getStatus() == RunningStatus.INIT,
                    "Only can insert before an INIT phase");
        }

        if(phase.getSeq() != FIRST) {
            phase.setPlannedStartDate(allPhases.get(phase.getSeq() - 1)
                    .getPlannedEndDate().plusDays(1));
        }
        validateBeforeSave(phase);
        List<Phase> laterImpactedPhases = new LinkedList<>(allPhases.subList(phase.getSeq(), allPhases.size()));

        allPhases.add(phase.getSeq(), phase);
        resetSeq(allPhases);
        movePhases(phase.getPeriod(), laterImpactedPhases);
        Phase createPhase = phaseRepository.save(phase);
        eventService.emit(Event.builder()
                .key(PROJECT_SCHEDULE_PHASE_ADDED)
                .source(createPhase.getId())
                .args(project.getName(),
                        createPhase.getName(),
                        createPhase.getPlannedStartDate().toString(),
                        createPhase.getPlannedEndDate().toString())
                .build());
        return createPhase;
    }

    @HandleBeforeSave
    public void preHandlePhaseSave(Phase phase) {
        Phase previousVersion = auditService.getPreviousVersion(phase)
                .orElseThrow(() -> new IllegalStateException("Previous version of phase does not exist."));
        Assert.isTrue(!PropertyChanged.isChange(previousVersion, phase, Phase.Fields.seq),
                "Seq can not be change.");

        validateBeforeSave(phase);
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
                checkOverflow(previousPhase);
                eventService.emit(Event.builder()
                        .key(Event.PROJECT_SCHEDULE_PHASE_END_CHANGED)
                        .source(previousPhase.getId())
                        .args(previousPhase.getProject().getName(),
                                previousPhase.getName(),
                                previousPlannedStartDate.toString(),
                                previousPhase.getPlannedEndDate().toString())
                        .build());
            }

            // Change planned start date also need update time frame of it's workItems
            Period offset = Period.between(previousVersion.getPlannedStartDate(), phase.getPlannedStartDate());
            moveWorkItemsInPhase(offset,phase);
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
        if(PropertyChanged.isChange(previousVersion, phase, Phase.Fields.name)) {
            eventService.emit(Event.builder()
                    .key(Event.PROJECT_PHASE_PROPERTIES_CHANGE)
                    .source(phase.getId())
                    .args(phase.getProject().getName(), phase.getName(),
                            PropertiesChanged.builder()
                                    .append(Phase.Fields.name, previousVersion.getName(), phase.getName())
                    )
                    .build());
        }
        if(PropertyChanged.isChange(previousVersion, phase, Phase.Fields.plannedStartDate)) {
            eventService.emit(Event.builder()
                    .key(Event.PROJECT_SCHEDULE_PHASE_START_CHANGED)
                    .source(phase.getId())
                    .args(phase.getProject().getName(), phase.getName(),
                            previousVersion.getPlannedStartDate().toString(),
                            phase.getPlannedStartDate().toString())
                    .build());
        }
        if(PropertyChanged.isChange(previousVersion, phase, Phase.Fields.plannedEndDate)) {
            eventService.emit(Event.builder()
                    .key(Event.PROJECT_SCHEDULE_PHASE_END_CHANGED)
                    .source(phase.getId())
                    .args(phase.getProject().getName(), phase.getName(),
                            previousVersion.getPlannedEndDate().toString(),
                            phase.getPlannedEndDate().toString())
                    .build());
        }
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
            moveWorkItemsInPhase(offset, phase);
            eventService.emit(Event.builder()
                    .key(eventKey)
                    .source(phase.getId())
                    .args(phase.getProject().getName(), phase.getName(), Math.abs(offset.getDays()))
                    .build());
        });
    }


    private void validateBeforeSave(Phase phase) {
        Assert.notNull(phase.getPlannedStartDate(), "Planned start date of first phase can not be null.");
        Assert.isTrue(phase.getPlannedStartDate().isBefore(phase.getPlannedEndDate()),
                "Planned start date must before planned end date");
        Assert.notNull(phase.getProject(), "Phase.project can not be null");
    }

    public void startPhase(long id) {
        Phase phase = phaseRepository.getOne(id);
        phase.start();
        eventService.emit(Event.builder()
                .key(PROJECT_EXECUTION_PHASE_START)
                .source(phase.getId())
                .args(phase.getProject().getName(), phase.getName())
                .build());
    }

    public void cancelPhase(long id) {
        Phase phase = phaseRepository.getOne(id);
        cancelWorkItems(phase);
        phase.cancel();
        eventService.emit(Event.builder()
                .key(PROJECT_EXECUTION_PHASE_CANCEL)
                .source(phase.getId())
                .args(phase.getProject().getName(), phase.getName())
                .build());
    }

    private void cancelWorkItems(Phase phase) {
        //TODO
    }

    private void deleteWorkItems(Phase phase) {
        //TODO
    }

    /**
     * Check all workItems, mark overflow flag if any workItem was over range of current Phase
     * @param phase
     */
    private void checkOverflow(Phase phase) {
        // TODO if we have workItems
    }

    /**
     * Move time frame of all workItems in phase
     * @param offSet
     * @param phase
     */
    private void moveWorkItemsInPhase(Period offSet, Phase phase) {
        // TODO if we have workItems
    }
}
