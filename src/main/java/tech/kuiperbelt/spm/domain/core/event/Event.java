package tech.kuiperbelt.spm.domain.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.*;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;
import tech.kuiperbelt.spm.domain.core.support.UserContext;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event extends BaseEntity {
    /**
     * Project
     */

    public static final String PROJECT_ADDED = "event.project.added";
    public static final String PROJECT_DELETED = "event.project.deleted";
    public static final String PROJECT_STARTED = "event.project.started";
    public static final String PROJECT_DONE = "event.project.done";
    public static final String PROJECT_CANCELED = "event.project.canceled";

    public static final String PROJECT_OWNER_CHANGED = "event.project.owner.changed";
    public static final String PROJECT_MANAGER_CHANGED = "event.project.manager.changed";
    public static final String PROJECT_MEMBER_ADDED = "event.project.member.added";
    public static final String PROJECT_MEMBER_DELETED = "event.project.member.deleted";
    public static final String PROJECT_PROPERTIES_CHANGED = "event.project.properties.changed";



    /**
     * Phase
     */
    public static final String PHASE_ADDED = "event.phase.added";
    public static final String PHASE_DELETED = "event.phase.deleted";
    public static final String PHASE_STARTED = "event.phase.started";
    public static final String PHASE_DONE = "event.phase.done";
    public static final String PHASE_CANCELED = "event.phase.canceled";

    public static final String PHASE_PROPERTIES_CHANGED = "event.phase.properties.changed";
    public static final String PHASE_MOVED_LEFT = "event.phase.moved.left";
    public static final String PHASE_MOVED_RIGHT = "event.phase.moved.right";
    public static final String PHASE_START_CHANGED = "event.phase.start.changed";
    public static final String PHASE_END_CHANGED = "event.phase.end.changed";



    /**
     * WorkItem
     */

    public static final String PROJECT_ITEM_ADDED = "event.project.item.added";
    public static final String PHASE_ITEM_ADDED = "event.phase.item.added";
    public static final String ITEM_ADDED = "event.item.added";
    public static final String ITEM_DELETED = "event.item.deleted";

    public static final String ITEM_OWNER_CHANGED = "event.item.owner.changed";
    public static final String ITEM_ASSIGNEE_CHANGED = "event.item.assignee.changed";
    public static final String ITEM_PROPERTIES_CHANGED = "event.item.properties.changed";
    public static final String ITEM_OVERFLOW_TRUE = "event.item.overflow.true";
    public static final String ITEM_READY_TRUE = "event.item.ready.true";

    public static final String ITEM_MOVED_LEFT = "event.item.moved.left";
    public static final String ITEM_MOVED_RIGHT = "event.item.moved.right";
    public static final String ITEM_START_CHANGED = "event.item.start.changed";
    public static final String ITEM_END_CHANGED = "event.item.end.changed";
    public static final String ITEM_PHASE_CHANGED = "event.item.phase.changed";
    public static final String ITEM_PROJECT_CHANGED = "event.item.project.changed";

    public static final String ITEM_STARTED = "event.item.started";
    public static final String ITEM_DONE = "event.item.done";
    public static final String ITEM_CANCELED = "event.item.canceled";

    /**
     * Note
     */
    public static final String ITEM_NOTE_TAKEN = "event.item.note.taken";
    public static final String PHASE_NOTE_TAKEN = "event.phase.note.taken";
    public static final String PROJECT_NOTE_TAKEN = "event.project.note.taken";
    public static final String NOTE_DELETED = "event.note.deleted";

    public static Signal BULK_BEGIN = new Signal();
    public static Signal BULK_END = new Signal();

    @JsonIgnore
    private String correlationId;

    @JsonIgnore
    @Transient
    private UserContext userContext;

    @ToString.Include
    private String key;

    private String triggeredMan;

    @JsonIgnore
    private Long source;

    @Transient
    private String detail;

    private LocalDateTime timestamp;

    @ToString.Include
    private String args;

    @JsonIgnore
    public Object[] getArgs() {
        if(args == null) {
            return new Object[0];
        }
        return new Gson().fromJson(args, Object[].class);
    }

    public void setArgs(Object... args) {
        this.args = new Gson().toJson(args);
    }

    public static class EventBuilder {
        private String args;
        private Long source;

        public EventBuilder args(Object... args) {
            GsonBuilder gsonBuildr = new GsonBuilder();
            gsonBuildr.registerTypeAdapter(PropertyChanged.class, new PropertyChanged.JsonSerializer());
            this.args = gsonBuildr.create().toJson(args);
            return this;
        }

        public EventBuilder source(Long sourceId) {
            this.source = sourceId;
            return this;
        }

        public EventBuilder source(BaseEntity entity) {
            Assert.notNull(entity, "Entity can not be null");
            return source(entity.getId());
        }
    }

    public static class Signal {}

    public static class EventQueue extends LinkedList<Event> implements Queue<Event> {}
}



