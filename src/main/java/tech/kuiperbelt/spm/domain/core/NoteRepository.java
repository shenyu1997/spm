package tech.kuiperbelt.spm.domain.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByParent(@Param("parent") Long parent);

    void deleteByParent(@Param("parent") Long parent);
}
