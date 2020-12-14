package tech.kuiperbelt.spm.domain.message;

import lombok.Setter;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;
import tech.kuiperbelt.spm.domain.core.support.SpmRepositoryControllerSupport;

import javax.annotation.Nonnull;

@Setter
@RepositoryRestController
@RequestMapping("/messages")
public class MessageController extends SpmRepositoryControllerSupport {

    public static class MessageRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Message>> {

        private final IdMappingService idMappingService;

        public MessageRepresentationModelProcessor(IdMappingService idMappingService) {
            this.idMappingService = idMappingService;
        }

        @Nonnull
        @Override
        public EntityModel<Message> process(EntityModel<Message> model) {
            Message message = model.getContent();
            Assert.notNull(message, "message can not be null");

            idMappingService.toEntityIdsLink(message.getEvents(), "events")
                    .ifPresent(model::add);

            idMappingService
                    .toEntityLink(message.getSource())
                    .ifPresent(model::add);

            return model;
        }

    }
}
