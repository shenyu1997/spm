package tech.kuiperbelt.spm.domain.event;

import lombok.*;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
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

    private String correlationId;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    private EventType type;

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



