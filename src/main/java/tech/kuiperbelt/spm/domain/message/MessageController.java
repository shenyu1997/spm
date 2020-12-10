package tech.kuiperbelt.spm.domain.message;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;

import javax.annotation.Nonnull;

@Setter
@RepositoryRestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    public static class MessageRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Message>> {

        private final IdMappingService idMappingService;

        public MessageRepresentationModelProcessor(IdMappingService idMappingService) {
            this.idMappingService = idMappingService;
        }

        @Nonnull
        @Override
        public EntityModel<Message> process(EntityModel<Message> model) {
            Message message = model.getContent();
            Assert.notNull(message, "Event can not be null");
            idMappingService
                    .toEntityLink(message.getSource())
                    .ifPresent(model::add);
            return model;
        }
    }
}
