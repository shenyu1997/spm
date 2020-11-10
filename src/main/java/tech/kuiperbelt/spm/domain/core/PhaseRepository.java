package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RepositoryRestResource
public interface PhaseRepository extends JpaRepository<Phase, Long> {

    default Optional<Phase> findLastPhase(Project project) {
        return findTop1ByProjectOrderBySeqDesc(project);
    }

    Optional<Phase> findTop1ByProjectOrderBySeqDesc(Project project);
}
