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
public interface NoteRepository extends JpaRepository<Note, Long>, FindByIds<Note, Long> {
    List<Note> findByParent(@Param("parent") Long parent);

    void deleteByParent(@Param("parent") Long parent);

    @Override
    @Query("from Note n where n.id in (:ids)")
    List<Note> findByIds(@Param("ids") List<Long> ids);
}
