package tech.kuiperbelt.spm.domain.idmapping;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "id_mappings")
public class IdMapping {
    @Id
    private Long id;
    private String type;
}
