package tech.kuiperbelt.spm.domain.core;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.rest.core.annotation.RestResource;
import tech.kuiperbelt.spm.domain.core.support.AuditDelegate;
import tech.kuiperbelt.spm.domain.core.support.AuditListener;
import tech.kuiperbelt.spm.domain.core.support.AuditableEntity;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;

import javax.persistence.*;
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

    @RestResource(path = "work-item")
    @ManyToOne
    private WorkItem workItem;

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();
}
