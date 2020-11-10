package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;

import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static tech.kuiperbelt.spm.domain.event.Event.PROJECT_SCHEDULE_PHASE_APPEND;

@Transactional
@Setter
@Service
@RepositoryEventHandler
public class PhaseService {

    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EventService eventService;

    @HandleBeforeDelete
    public void preHandlePhaseDelete(Phase phase) {
        List<Phase> phases = phase.getProject().getPhases();
        phases.remove(phase);
        rePlaning(phases.stream()
                .sorted(Comparator.comparing(Phase::getSeq))
                .collect(Collectors.toList()));
    }

    private void rePlaning(List<Phase> phases) {
        if(CollectionUtils.isEmpty(phases)) {
            return;
        }

        // Set first sequence
        phases.get(0).setSeq(0);
        if(phases.size() == 1) {
            return;
        }

        // Reset planned start date, planned end data
        for(int i=1; i<phases.size(); i++) {
            Phase previous = phases.get(i - 1);
            Phase current = phases.get(i);
            current.setSeq(i);
            Period period = current.getPeriod();
            current.setPlannedStartDate(previous.getPlannedEndDate().plusDays(1));
            current.setPlannedEndDate(current.getPlannedStartDate().plus(period));
        }
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

    public void appendPhase(Long projectId, Phase phase) {
        Project project = projectService.getProjectById(projectId);
        phase.setProject(project);
        phase.setStatus(RunningStatus.INIT);

        Optional<Phase> lastPhaseOp = phaseRepository.findLastPhase(project);
        if(lastPhaseOp.isPresent()) {
            // Add to tail if not first phase
            Phase lastPhase = lastPhaseOp.get();
            phase.setSeq(lastPhase.getSeq() + 1);
            phase.setPlannedStartDate(lastPhase.getPlannedEndDate().plusDays(1));
        } else {
            // Add to head if is first
            phase.setSeq(0);
        }
        validateBeforeSave(phase);
        phaseRepository.save(phase);

        eventService.emit(Event.builder()
                .key(PROJECT_SCHEDULE_PHASE_APPEND)
                .source(phase.getId())
                .args(project.getName(),
                        phase.getName(),
                        phase.getPlannedStartDate().toString(),
                        phase.getPlannedEndDate().toString())
                .build());
    }

    private void validateBeforeSave(Phase phase) {
        if(phase.getSeq() == 0) {
            Assert.notNull(phase.getPlannedStartDate(), "Planned start date of first phase can not be null.");
        }
        Assert.isTrue(phase.getPlannedStartDate().isBefore(phase.getPlannedEndDate()),
                "Planned start date must before planned end date");
    }
}
