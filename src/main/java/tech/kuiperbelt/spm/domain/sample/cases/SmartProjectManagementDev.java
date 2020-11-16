package tech.kuiperbelt.spm.domain.sample.cases;

import com.google.common.collect.Lists;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.PhaseService;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.ProjectService;
import tech.kuiperbelt.spm.domain.sample.ImportCase;

import java.time.LocalDate;
import java.time.Period;

@Setter
@Component
public class SmartProjectManagementDev extends ImportCaseSupport implements ImportCase {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PhaseService phaseService;


    @Transactional
    @Override
    public void importDate() {
        Long projectId = step("Create project", () ->
            projectService.createProject(new Project().toBuilder()
                    .name("Smart Project Management DEV")
                    .members(Lists.newArrayList("huanhuan.dong", "yongjian.sha"))
                    .build()).getId()
        );

        step("Add Phase 1", () -> {
            projectService.appendPhase(projectId, new Phase().toBuilder()
                    .name("Planning")
                    .plannedStartDate(LocalDate.now())
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(30)))
                    .build());
        });

        step("Add Phase 2", () -> {
            projectService.appendPhase(projectId, new Phase().toBuilder()
                    .name("GoGoGo")
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(80)))
                    .build());
        });

        step("Add Phase 3", () -> {
            projectService.appendPhase(projectId, new Phase().toBuilder()
                    .name("Testing")
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(120)))
                    .build());
        });

        step("Add Phase 4", () -> {
            projectService.appendPhase(projectId, new Phase().toBuilder()
                    .name("GoProduction")
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(160)))
                    .build());
        });

        step("Start project", () -> {
            projectService.startProject(projectId);
        });
    }


}
