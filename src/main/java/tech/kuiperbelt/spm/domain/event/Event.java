package tech.kuiperbelt.spm.domain.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.*;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.common.BaseEntity;
import tech.kuiperbelt.spm.common.UserContext;

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
    public static final String PROJECT_REMOVED = "event.project.removed";

    public static final String PROJECT_OWNER_CHANGED = "event.project.owner.changed";
    public static final String PROJECT_MANAGER_CHANGED = "event.project.manager.changed";
    public static final String PROJECT_MEMBER_ADDED = "event.project.member.added";
    public static final String PROJECT_MEMBER_REMOVED = "event.project.member.removed";

    public static final String PROJECT_PROPERTIES_CHANGE = "event.project.properties.change"; //TODO
    public static final String PROJECT_PHASE_PROPERTIES_CHANGE = "event.project.phase.properties.change"; //TODO

    public static final String PROJECT_SCHEDULE_PHASE_ADDED = "event.project.schedule.phase.added";
    public static final String PROJECT_SCHEDULE_PHASE_REMOVED = "event.project.schedule.phase.removed";
    public static final String PROJECT_SCHEDULE_PHASE_MOVED_LEFT = "event.project.schedule.phase.moved.left"; //TODO
    public static final String PROJECT_SCHEDULE_PHASE_MOVED_RIGHT = "event.project.schedule.phase.moved.right"; //TODO
    public static final String PROJECT_SCHEDULE_PHASE_START_CHANGED = "event.project.schedule.phase.start.change"; //TODO
    public static final String PROJECT_SCHEDULE_PHASE_END_CHANGED = "event.project.schedule.phase.end.change";//TODO
    public static final String PROJECT_SCHEDULE_PHASE_REMOVE = "event.project.schedule.phase.remove"; //TODO

    public static final String PROJECT_EXECUTION_PROJECT_START = "event.project.execution.project.start"; //TODO
    public static final String PROJECT_EXECUTION_PROJECT_DONE = "event.project.execution.project.done";//TODO
    public static final String PROJECT_EXECUTION_PROJECT_CANCELED = "event.project.execution.project.canceled";

    public static final String PROJECT_EXECUTION_PHASE_START = "event.project.execution.phase.start"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_DONE = "event.project.execution.phase.done"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_CANCEL = "event.project.execution.phase.cancel"; //TODO

    public static final String DETACH_ITEM_ADDED = "event.detach.item.added";//TODO

    public static final String ITEM_ADDED = "event.item.added";//TODO
    public static final String ITEM_REMOVE = "event.item.removed";//TODO

    public static final String ITEM_OWNER_CHANGED = "event.item.owner.changed";//TODO
    public static final String ITEM_ASSIGNEE_CHANGED = "event.item.assignee.changed";//TODO
    public static final String ITEM_PROPERTIES_CHANGE = "event.project.properties.change"; //TODO

    public static final String ITEM_SCHEDULE_MOVE_LEFT = "event.item.schedule.move.left";//TODO
    public static final String ITEM_SCHEDULE_MOVE_RIGHT = "event.item.schedule.move.right";//TODO
    public static final String ITEM_SCHEDULE_MOVE_PHASE = "event.item.schedule.move.phase";//TODO
    public static final String ITEM_SCHEDULE_START_CHANGED = "event.item.schedule.start.change";//TODO
    public static final String ITEM_SCHEDULE_END_CHANGED = "event.item.schedule.end.change";//TODO

    public static final String ITEM_SCHEDULE_OVERFLOW = "event.item.overflow";//TODO
    public static final String ITEM_SCHEDULE_IS_READY = "event.item.schedule.ready"; //TODO

    public static final String ITEM_EXECUTION_START = "event.item.execution.start"; //TODO
    public static final String ITEM_EXECUTION_DONE = "event.item.execution.done"; //TODO
    public static final String ITEM_EXECUTION_CANCEL = "event.item.execution.cancel"; //TODO

    public static final String ITEM_EXECUTION_NOTE_TAKE = "event.item.execution.note.take"; //TODO
    public static final String ITEM_EXECUTION_NOTE_DELETE = "event.item.execution.note.delete"; //TODO

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



