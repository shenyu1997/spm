package tech.kuiperbelt.spm.domain.message;

import tech.kuiperbelt.spm.domain.core.support.BaseEntity;
import tech.kuiperbelt.spm.domain.core.event.Event;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {
    private boolean isRead;
    private String receiver;
    @ManyToMany
    private List<Event> events;
}
