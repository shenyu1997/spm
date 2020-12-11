package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import tech.kuiperbelt.spm.domain.core.idmapping.FindByIds;

import java.util.List;

@Repository
@RepositoryRestResource
public interface PhaseRepository extends JpaRepository<Phase, Long>, FindByIds<Phase, Long> {
    @Override
    @Query("from Phase p where p.id in (:ids)")
    List<Phase> findByIds(@Param("ids") List<Long> ids);
}
