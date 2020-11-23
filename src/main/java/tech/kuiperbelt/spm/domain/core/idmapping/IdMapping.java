package tech.kuiperbelt.spm.domain.core.idmapping;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@FieldNameConstants
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = IdMapping.ID_MAPPINGS)
public class IdMapping {
    public static final String ID_MAPPINGS = "id_mappings";
    @Id
    private Long id;
    private String type;
}
