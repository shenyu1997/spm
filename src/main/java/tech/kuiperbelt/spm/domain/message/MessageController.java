package tech.kuiperbelt.spm.domain.message;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tech.kuiperbelt.spm.domain.core.event.EventController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Setter
@RepositoryRestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private EventController eventController;

    @RequestMapping(method = RequestMethod.GET, value = "/messages/{id}/events")
    public ResponseEntity<CollectionModel<EventController.Event>> getMessageEvents(@PathVariable("id") Long id) {
        List<EventController.Event> collection = messageService
                .findMessageEvents(id).stream()
                .map(eventController::buildEventResource)
                .collect(Collectors.toList());
        CollectionModel<EventController.Event> collectionModel = CollectionModel.of(collection);
        collectionModel.add(linkTo(methodOn(MessageController.class).getMessageEvents(id)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

}
