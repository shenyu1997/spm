package tech.kuiperbelt.spm.domain.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByReceiver(String upn, Pageable pageable);
}
