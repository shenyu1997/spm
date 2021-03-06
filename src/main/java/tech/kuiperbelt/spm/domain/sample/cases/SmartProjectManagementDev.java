package tech.kuiperbelt.spm.domain.sample.cases;

import com.google.common.collect.Lists;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.domain.core.*;
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

    @Autowired
    private WorkItemService workItemService;


    @Transactional
    @Override
    public void importDate() {
        Long projectId = step("Create project", () ->
            projectService.createProject(new Project().toBuilder()
                    .name("Smart Project Management DEV")
                    .manager("huanhuan.dong")
                    .members(Lists.newArrayList("huanhuan.dong", "yongjian.sha"))
                    .build()).getId()
        );

        long phaseOneId = step("Add Phase 1", () -> {
            return projectService.createPhase(projectId, new Phase().toBuilder()
                    .name("Planning")
                    .plannedStartDate(LocalDate.now())
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(30)))
                    .build()).getId();
        });

        long phaseTwoId = step("Add Phase 2", () -> {
            return projectService.createPhase(projectId, new Phase().toBuilder()
                    .name("GoGoGo")
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(80)))
                    .build()).getId();
        });

        long phaseThreeId = step("Add Phase 3", () -> {
            return projectService.createPhase(projectId, new Phase().toBuilder()
                    .name("Testing")
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(120)))
                    .build())
                    .getId();
        });

        long phaseFourId = step("Add Phase 4", () -> {
            return projectService.createPhase(projectId, new Phase().toBuilder()
                    .name("GoProduction")
                    .plannedEndDate(LocalDate.now().plus(Period.ofDays(160)))
                    .build())
                    .getId();
        });

        step("Start project", () -> {
            projectService.startProject(projectId);
        });

        long workItemProjectAId = step("Add workItems A belong to Project Planning Phase", () -> {
            return projectService.createDirectWorkItem(projectId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("WorkItems belong to Project Planning")
                    .detail("Just for testing")
                    .priority(WorkItem.Priority.MEDIUM)
                    .plannedStartDate(LocalDate.now().plusDays(1))
                    .deadLine(LocalDate.now().plusDays(30))
                    .build()).getId();
        });

        long workItemProjectBId = step("Add workItems B belong to Project Planning Phase", () -> {
            return projectService.createDirectWorkItem(projectId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("WorkItems belong to Project Planning")
                    .detail("Just for testing, 2")
                    .priority(WorkItem.Priority.TOP)
                    .plannedStartDate(LocalDate.now().plusDays(5))
                    .deadLine(LocalDate.now().plusDays(6))
                    .build()).getId();
        });

        long workItemHLDId = step("Add workItems(Write Design doc) to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("Write Design doc (HLD)")
                    .detail("Write Design doc (HLD), include data model, use cases...")
                    .priority(WorkItem.Priority.HIGH)
                    .plannedStartDate(LocalDate.now().plusDays(1))
                    .deadLine(LocalDate.now().plusDays(30))
                    .build()).getId();
        });

        long workItemReviewHDLId = step("Review Design doc (HLD) to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("Review Design doc (HLD)")
                    .detail("Modify HLD after Server Poc finished.")
                    .priority(WorkItem.Priority.LOW)
                    .plannedStartDate(LocalDate.now().plusDays(30))
                    .deadLine(LocalDate.now().plusDays(35))
                    .build()).getId();
        });

        long workItemCollectUIDesignIdeaId = step("Collect UI Design Idea to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("huanhuan.dong")
                    .name("Collect UI Design Idea")
                    .detail("Find system/software in market, to find hot idea of project mangement")
                    .plannedStartDate(LocalDate.now().plusDays(1))
                    .deadLine(LocalDate.now().plusDays(30))
                    .build()).getId();
        });

        long workItemServicePOCId = step("Service POC to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("Service POC")
                    .detail("Draft implement MVP of HLD, for server side, to choose basic tech framework and tech stack")
                    .plannedStartDate(LocalDate.now().plusDays(15))
                    .deadLine(LocalDate.now().plusDays(35))
                    .build()).getId();
        });

        long workItemClientUIId = step("Client UI POC to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("huanhuan.dong")
                    .name("Client UI POC")
                    .detail("Draft implement MVP of HLD, for client side, to choose basic tech framework and tech stack")
                    .plannedStartDate(LocalDate.now().plusDays(45))
                    .build()).getId();
        });

        long workItemCollectId = step("Client UI POC to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("yongjian.sha")
                    .name("Collect requirement from real customer")
                    .detail("Touch real customer, collect requirement")
                    .priority(WorkItem.Priority.HIGH)
                    .plannedStartDate(LocalDate.now().plusDays(1))
                    .deadLine(LocalDate.now().plusDays(40))
                    .build()).getId();
        });

        long workItemDiscussMvpId = step("Discuss MVP to Planning Phase", () -> {
            return phaseService.createWorkItem(phaseOneId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("Discuss MVP, confirm minimal scope")
                    .detail("Discuss MVP, talk about it within team members")
                    .milestone(true)
                    .plannedStartDate(LocalDate.now().plusDays(40))
                    .deadLine(LocalDate.now().plusDays(45))
                    .build()).getId();
        });

        long workItemMonitorId = step("Monitor Planning Phase", () -> {
            return phaseService.createWorkItem(phaseFourId, new WorkItem().toBuilder()
                    .assignee("yu.shen")
                    .name("Monitor production ENV")
                    .detail("Now, it is real cake off, we start from here.")
                    .milestone(true)
                    .plannedStartDate(LocalDate.now().plusDays(40))
                    .deadLine(LocalDate.now().plusDays(45))
                    .build()).getId();
        });

        step("Start workItem Write Design doc", () -> {
            workItemService.startWorkItem(workItemHLDId);
        });

        step("Start workItem Write Design doc", () -> {
            workItemService.startWorkItem(workItemServicePOCId);
        });

        step("Take a note on workItemHLDId", () -> {
            workItemService.takeNote(workItemHLDId, new Note().toBuilder()
                    .content("Please update status")
                    .build());

            workItemService.takeNote(workItemHLDId, new Note().toBuilder()
                    .content("Hi guy, update status, please")
                    .build());
        });

        step("Take a note on Client UI POC", () -> {
            workItemService.takeNote(workItemClientUIId, new Note().toBuilder()
                    .content("I am going to do it.")
                    .build());

            workItemService.takeNote(workItemClientUIId, new Note().toBuilder()
                    .content("It's hard, but I am going to do it really.")
                    .build());
        });
    }
}
