package tech.kuiperbelt.spm.domain.event;

import com.google.gson.Gson;
import lombok.*;
import tech.kuiperbelt.spm.common.BaseEntity;

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
    public static final String PROJECT_CANCELED = "event.project.canceled";
    public static final String PROJECT_REMOVED = "event.project.removed";
    public static final String PROJECT_START = "event.project.start"; //TODO
    public static final String PROJECT_DONE = "event.project.done";//TODO

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

    public static final String PROJECT_EXECUTION_PHASE_START = "event.project.execution.phase.start"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_DONE = "event.project.execution.phase.done"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_CANCEL = "event.project.execution.phase.cancel"; //TODO
    public static final String PROJECT_EXECUTION_PHASE_REMOVE = "event.project.execution.phase.remove"; //TODO


    public static Signal BULK_BEGIN = new Signal();
    public static Signal BULK_END = new Signal();


    private String correlationId;

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

        public EventBuilder args(Object... args) {
            this.args = new Gson().toJson(args);
            return this;
        }
    }

    public static class Signal {}

    public static class EventQueue extends LinkedList<Event> implements Queue<Event> {}
}



