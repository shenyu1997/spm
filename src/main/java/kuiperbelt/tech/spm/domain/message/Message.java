package kuiperbelt.tech.spm.domain.message;

import kuiperbelt.tech.spm.common.BaseEntity;
import kuiperbelt.tech.spm.domain.event.Event;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Message extends BaseEntity {
    private boolean isRead;
    private String receiver;
    @ManyToMany
    private List<Event> events;
}
