package tech.kuiperbelt.spm.domain.core.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import tech.kuiperbelt.spm.domain.core.idmapping.FindByIds;

import java.util.List;

@Repository
@RepositoryRestResource
public interface EventRepository extends JpaRepository<Event, Long>, FindByIds<Event, Long> {

    @Override
    @Query("from Event e where e.id in (:ids)")
    List<Event> findByIds(@Param("ids") List<Long> longs);
}
