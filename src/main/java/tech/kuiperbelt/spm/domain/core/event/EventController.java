package tech.kuiperbelt.spm.domain.core.event;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;

@Setter
@RepositoryRestController
public class EventController {

    @Autowired
    private EventService eventService;



    public static class EventRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Event>> {

        private final EventService eventService;

        private final IdMappingService idMappingService;

        public EventRepresentationModelProcessor(EventService eventService, IdMappingService idMappingService) {
            this.eventService = eventService;
            this.idMappingService = idMappingService;
        }

        @Override
        public EntityModel<Event> process(EntityModel<Event> model) {
            Event event = model.getContent();
            Assert.notNull(event, "Event can not be null");
            eventService.enhance(event);
            idMappingService
                    .toEntityLink(event.getSource())
                    .ifPresent(model::add);
            return model;
        }
    }
}
