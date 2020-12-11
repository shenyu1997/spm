package tech.kuiperbelt.spm.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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

    @JsonIgnore
    private Long source;

    @JsonIgnore
    @ElementCollection
    private List<Long> events;
}
