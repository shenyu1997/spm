package tech.kuiperbelt.spm.domain.event;

import tech.kuiperbelt.spm.common.BaseEntity;
import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Event extends BaseEntity {
    public final static String ENTITY_TYPE = "Event";

    private String correlationId;

    private Type type;

    private String subType;

    private String triggeredMan;

    private Long source;

    @Transient
    private String content;

    private LocalDateTime timestamp;

    @ElementCollection
    private List<String> args;


    public enum Type {
        INFORMATION_CHANGED, PARTICIPANT_CHANGED, SCHEDULE_CHANGED, EXECUTION_STATUS_CHANGED, OTHER, SYSTEM_BULK_END
    }
}



