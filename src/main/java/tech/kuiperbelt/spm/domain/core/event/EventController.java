package tech.kuiperbelt.spm.domain.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
public class EventController {

    @Autowired
    private EventService eventService;

    @RequestMapping(method = RequestMethod.GET, value = "/events/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id")Long id) {
        return eventService.findEventById(id)
                .map(this::buildEventResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public Event buildEventResource(tech.kuiperbelt.spm.domain.core.event.Event event) {
        Event eventResource = new Event(event);
        eventResource.add(linkTo(methodOn(EventController.class).getEvent(event.getId())).withSelfRel());
        return eventResource;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Event extends RepresentationModel<Event> {


        @JsonIgnore
        @Delegate
        private tech.kuiperbelt.spm.domain.core.event.Event event;
    }

}
