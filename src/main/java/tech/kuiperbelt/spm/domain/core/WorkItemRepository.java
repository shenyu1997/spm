package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(path = WorkItemRepository.PATH_WORK_ITEMS)
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    String PATH_WORK_ITEMS = "work-items";

    Page<WorkItem> findByAssignee(@Param("assignee") String assignee, Pageable pageable);

    Page<WorkItem> findByOwner(@Param("owner") String owner, Pageable pageable);

}
