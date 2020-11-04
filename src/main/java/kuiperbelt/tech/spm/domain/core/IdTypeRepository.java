package kuiperbelt.tech.spm.domain.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdTypeRepository extends JpaRepository<IdType, Long> {
    IdType findByTarget(Long target);
}
