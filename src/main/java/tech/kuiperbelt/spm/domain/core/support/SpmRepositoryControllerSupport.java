package tech.kuiperbelt.spm.domain.core.support;

import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.http.ResponseEntity;
import tech.kuiperbelt.spm.domain.core.WorkItem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public abstract class SpmRepositoryControllerSupport {
    protected ResponseEntity<CollectionModel<?>> assembleCollectionResource(PersistentEntityResourceAssembler persistentEntityResourceAssembler,
                                                                  ResponseEntity<CollectionModel<?>> selfMethod,
                                                                  Supplier<Collection<?>> collectionProvider) {
        List<PersistentEntityResource> collection = collectionProvider.get().stream()
                .map(persistentEntityResourceAssembler::toModel)
                .collect(Collectors.toList());
        CollectionModel<?> collectionModel;
        if(collection.isEmpty()) {
            EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
            EmbeddedWrapper wrapper = wrappers.emptyCollectionOf(WorkItem.class);
            collectionModel = CollectionModel.of(Collections.singleton(wrapper));
        } else {
            collectionModel = CollectionModel.of(collection);
        }
        collectionModel.add(linkTo(selfMethod).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }
}
