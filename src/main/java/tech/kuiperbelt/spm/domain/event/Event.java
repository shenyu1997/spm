package tech.kuiperbelt.spm.domain.event;

import lombok.*;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event extends BaseEntity {

    public static final String EVENT_PROJECT_CREATED = "event.project.created";
    public static final String EVENT_PROJECT_CANCELED = "event.project.canceled";
    public static final String EVENT_PROJECT_REMOVED = "event.project.removed";
    public static final String EVENT_PROJECT_OWNER_CHANGED = "event.project.owner.changed";
    public static final String EVENT_PROJECT_MANAGER_CHANGED = "event.project.manager.changed";
    public static final String EVENT_PROJECT_NAME_CHANGE = "event.project.name.change";
    public static final String EVENT_PROJECT_MEMBER_ADDED = "event.project.member.added";
    public static final String EVENT_PROJECT_MEMBER_REMOVED = "event.project.member.removed";

    public enum Type {
        INFORMATION_CHANGED, PARTICIPANT_CHANGED, SCHEDULE_CHANGED, EXECUTION_STATUS_CHANGED, OTHER, SYSTEM_BULK_END
    }

    private String correlationId;

    private Type type;

    @ToString.Include
    private String key;

    private String triggeredMan;

    private Long source;

    @Transient
    private String content;

    private LocalDateTime timestamp;

    @ToString.Include
    @ElementCollection
    private List<String> args;

    public static class EventBuilder {
        private List<String> args;

        public EventBuilder args(String... args) {
            this.args = new ArrayList();
            Stream.of(args).forEach(this.args::add);
            return this;
        }
    }
}



