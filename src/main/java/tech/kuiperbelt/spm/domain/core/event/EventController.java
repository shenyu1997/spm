package tech.kuiperbelt.spm.domain.core.event;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

@Setter
@RepositoryRestController
public class EventController {

    @Autowired
    private EventService eventService;



    public static class EventRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Event>> {

        private final EventService eventService;

        public EventRepresentationModelProcessor(EventService eventService) {
            this.eventService = eventService;
        }

        @Override
        public EntityModel<Event> process(EntityModel<Event> model) {
            eventService.enhance(model.getContent());
            return model;
        }
    }
}
