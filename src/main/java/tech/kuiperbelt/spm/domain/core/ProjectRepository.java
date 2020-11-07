package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;

@Repository
@RepositoryRestResource
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select distinct p from Project p left join p.members pms " +
            "where (p.owner=:me or p.manager=:me or :me in pms) " +
            "and p.status in (:status) ")
    Page<Project> findMyProjects(@Param("me") String me, @Param("status") EnumSet<RunningStatus> status, Pageable pageable);
}
