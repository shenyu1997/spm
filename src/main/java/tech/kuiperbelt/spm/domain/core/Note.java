package tech.kuiperbelt.spm.domain.core;


import lombok.*;
import lombok.experimental.FieldNameConstants;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@FieldNameConstants
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "notes")
public class Note extends BaseEntity {

    private String author;

    private String content;

    private LocalDate createDate;

    private LocalDateTime timestamp;

    @ManyToOne
    private WorkItem workItem;
}
