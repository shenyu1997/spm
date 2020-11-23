package tech.kuiperbelt.spm.domain.core.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface EventRepository extends JpaRepository<Event, Long> {
}
