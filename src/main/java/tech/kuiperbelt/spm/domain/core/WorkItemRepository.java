package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import tech.kuiperbelt.spm.domain.core.idmapping.FindByIds;

import java.util.List;

@Repository
@RepositoryRestResource(path = WorkItemRepository.PATH_WORK_ITEMS)
public interface WorkItemRepository extends JpaRepository<WorkItem, Long>, FindByIds<WorkItem, Long> {
    String PATH_WORK_ITEMS = "work-items";

    List<WorkItem> findByPhase(Phase phase);

    Page<WorkItem> findByAssignee(@Param("assignee") String assignee, Pageable pageable);

    Page<WorkItem> findByOwner(@Param("owner") String owner, Pageable pageable);

    @Query("from WorkItem w where w.owner=:me or w.assignee=:me")
    Page<WorkItem> findMyItems(@Param("me") String me, Pageable pageable);

    @Override
    @Query("from WorkItem wi where wi.id in (:ids)")
    List<WorkItem> findByIds(@Param("ids") List<Long> ids);

}
