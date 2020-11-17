package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface NoteRepository extends JpaRepository<Note, Long> {
}
