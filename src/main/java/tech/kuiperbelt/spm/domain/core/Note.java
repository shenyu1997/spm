package tech.kuiperbelt.spm.domain.core;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import tech.kuiperbelt.spm.domain.core.support.AuditDelegate;
import tech.kuiperbelt.spm.domain.core.support.AuditListener;
import tech.kuiperbelt.spm.domain.core.support.AuditableEntity;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import java.time.LocalDate;

@EntityListeners(AuditListener.class)
@FieldNameConstants
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@Table(name = "notes")
public class Note extends BaseEntity implements AuditableEntity{

    @ToString.Include
    private String content;

    private LocalDate createDate;

    private Long parent;

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    public void setParent(BaseEntity baseEntity) {
        this.parent = baseEntity.getId();
    }
}
