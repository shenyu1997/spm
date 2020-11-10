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
    public static final String EVENT_PROJECT_REMOVED = "event.project.removed";

    public static final String PROJECT_OWNER_CHANGED = "event.project.owner.changed";
    public static final String PROJECT_MANAGER_CHANGED = "event.project.manager.changed";
    public static final String PROJECT_MEMBER_ADDED = "event.project.member.added";
    public static final String PROJECT_MEMBER_REMOVED = "event.project.member.removed";

    public static final String PROJECT_PROPERTIES_NAME_CHANGE = "event.project.properties.name.change";

    public static final String PROJECT_SCHEDULE_PHASE_ADDED = "event.project.schedule.phase.added";
    public static final String PROJECT_SCHEDULE_PHASE_REMOVED = "event.project.schedule.phase.removed";

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



