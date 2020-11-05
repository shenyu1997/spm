package tech.kuiperbelt.spm.domain.idmapping;

import tech.kuiperbelt.spm.common.BaseEntity;
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
