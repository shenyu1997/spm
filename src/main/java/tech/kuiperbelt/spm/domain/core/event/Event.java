package tech.kuiperbelt.spm.domain.core.event;

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

    public static final String PROJECT_CREATED = "event.project.created";
    public static final String PROJECT_DELETED = "event.project.deleted";

    public static final String PROJECT_OWNER_CHANGED = "event.project.owner.changed";
    public static final String PROJECT_MANAGER_CHANGED = "event.project.manager.changed";
    public static final String PROJECT_MEMBER_ADDED = "event.project.member.added";
    public static final String PROJECT_MEMBER_DELETED = "event.project.member.deleted";

    public static final String PROJECT_PROPERTIES_CHANGED = "event.project.properties.changed"; //TODO
    public static final String PROJECT_PHASE_PROPERTIES_CHANGED = "event.project.phase.properties.changed"; //TODO

    public static final String PROJECT_SCHEDULE_PHASE_ADDED = "event.project.schedule.phase.added";
    public static final String PROJECT_SCHEDULE_PHASE_DELETED = "event.project.schedule.phase.deleted";
    public static final String PROJECT_SCHEDULE_PHASE_MOVED_LEFT = "event.project.schedule.phase.moved.left"; //TODO
    public static final String PROJECT_SCHEDULE_PHASE_MOVED_RIGHT = "event.project.schedule.phase.moved.right"; //TODO
    public static final String PROJECT_SCHEDULE_PHASE_START_CHANGED = "event.project.schedule.phase.start.changed"; //TODO
    public static final String PROJECT_SCHEDULE_PHASE_END_CHANGED = "event.project.schedule.phase.end.changed";//TODO

    public static final String PROJECT_EXECUTION_PROJECT_STARTED = "event.project.execution.project.started"; //TODO
    public static final String PROJECT_EXECUTION_PROJECT_DONE = "event.project.execution.project.done";//TODO
    public static final String PROJECT_EXECUTION_PROJECT_CANCELED = "event.project.execution.project.canceled";

    public static final String PROJECT_EXECUTION_PHASE_STARTED = "event.project.execution.phase.started"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_DONE = "event.project.execution.phase.done"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_CANCELED = "event.project.execution.phase.canceled"; //TODO

    public static final String DETACH_ITEM_ADDED = "event.detach.item.added";//TODO

    public static final String ITEM_ADDED = "event.item.added";//TODO
    public static final String ITEM_DELETED = "event.item.deleted";//TODO

    public static final String ITEM_OWNER_CHANGED = "event.item.owner.changed";//TODO
    public static final String ITEM_ASSIGNEE_CHANGED = "event.item.assignee.changed";//TODO
    public static final String ITEM_PROPERTIES_CHANGED = "event.project.properties.changed"; //TODO

    public static final String ITEM_SCHEDULE_MOVED_LEFT = "event.item.schedule.moved.left";//TODO
    public static final String ITEM_SCHEDULE_MOVED_RIGHT = "event.item.schedule.moved.right";//TODO
    public static final String ITEM_MOVED_PHASE = "event.item.schedule.moved.phase";//TODO
    public static final String ITEM_SCHEDULE_START_CHANGED = "event.item.schedule.start.changed";//TODO
    public static final String ITEM_SCHEDULE_END_CHANGED = "event.item.schedule.end.changed";//TODO

    public static final String ITEM_SCHEDULE_IS_OVERFLOW = "event.item.is.overflow";//TODO
    public static final String ITEM_SCHEDULE_IS_READY = "event.item.schedule.is.ready"; //TODO

    public static final String ITEM_EXECUTION_STARTED = "event.item.execution.started"; //TODO
    public static final String ITEM_EXECUTION_DONE = "event.item.execution.done"; //TODO
    public static final String ITEM_EXECUTION_CANCELED = "event.item.execution.canceled"; //TODO

    public static final String ITEM_EXECUTION_NOTE_TAKEN = "event.item.execution.note.taken"; //TODO
    public static final String ITEM_EXECUTION_NOTE_DELETED = "event.item.execution.note.deleted"; //TODO

    public static Signal BULK_BEGIN = new Signal();
    public static Signal BULK_END = new Signal();


    private String correlationId;

    @Transient
    private UserContext userContext;

    @ToString.Include
    private String key;

    private String triggeredMan;

    private Long source;

    @Transient
    private String content;

    private LocalDateTime timestamp;

    @ToString.Include
    private String args;

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



