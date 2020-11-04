package kuiperbelt.tech.spm.domain.core;

import kuiperbelt.tech.spm.common.BaseEntity;
import lombok.*;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdType extends BaseEntity {
    private Long target;
    private String type;
}
