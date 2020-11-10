package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.event.EventService;

import java.util.Optional;

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

    @HandleBeforeCreate
    public void preHandlePhaseCreate(Phase phase) {

    }

    @HandleAfterCreate
    public void postHandlePhaseCreate(Phase phase) {

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
