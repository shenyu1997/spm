package tech.kuiperbelt.spm.domain.idmapping;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdMapping {
    @Id
    private Long id;
    private String type;
}
