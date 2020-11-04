package kuiperbelt.tech.spm.domain.core;

import kuiperbelt.tech.spm.common.BaseEntity;
import lombok.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
