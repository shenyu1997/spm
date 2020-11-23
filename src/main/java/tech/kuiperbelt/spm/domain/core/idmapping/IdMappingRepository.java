package tech.kuiperbelt.spm.domain.core.idmapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdMappingRepository extends JpaRepository<IdMapping, Long> {

}
