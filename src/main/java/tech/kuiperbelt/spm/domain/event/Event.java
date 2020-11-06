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
    public final static String ENTITY_TYPE = "Event";

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

    public enum Type {
        INFORMATION_CHANGED, PARTICIPANT_CHANGED, SCHEDULE_CHANGED, EXECUTION_STATUS_CHANGED, OTHER, SYSTEM_BULK_END
    }

    public static class EventBuilder {
        private List<String> args;

        public EventBuilder args(String... args) {
            this.args = new ArrayList();
            Stream.of(args).forEach(this.args::add);
            return this;
        }
    }
}



