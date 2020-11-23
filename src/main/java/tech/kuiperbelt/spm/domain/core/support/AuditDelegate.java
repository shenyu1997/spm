package tech.kuiperbelt.spm.domain.core.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@ToString
public class AuditDelegate implements AuditableEntity{

    @Column(name = "operator")
    private String operator;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
