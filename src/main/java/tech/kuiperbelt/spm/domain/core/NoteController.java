package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;

@Setter
@RepositoryRestController
@RequestMapping("/notes")
public class NoteController {

    public static class NoteRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Note>> {

        private final IdMappingService idMappingService;

        public NoteRepresentationModelProcessor(IdMappingService idMappingService) {
            this.idMappingService = idMappingService;
        }

        @Override
        public EntityModel<Note> process(EntityModel<Note> model) {
            Note note = model.getContent();
            Assert.notNull(note, "Note can not be null");
            idMappingService
                    .toEntityLink(note.getParent())
                    .ifPresent(model::add);
            return model;
        }
    }
}
