package tech.kuiperbelt.spm.domain.core;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import tech.kuiperbelt.spm.common.AuditDelegate;
import tech.kuiperbelt.spm.common.AuditListener;
import tech.kuiperbelt.spm.common.AuditableEntity;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;

@EntityListeners(AuditListener.class)
@FieldNameConstants
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "notes")
public class Note extends BaseEntity implements AuditableEntity{

    private String content;

    private LocalDate createDate;

    @ManyToOne
    private WorkItem workItem;

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();
}
