package tech.kuiperbelt.spm.domain.event;

import com.google.gson.Gson;
import lombok.*;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event extends BaseEntity {

    public static Signal BULK_END = new Signal();

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
}



