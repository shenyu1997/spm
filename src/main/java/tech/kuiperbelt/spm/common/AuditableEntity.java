package tech.kuiperbelt.spm.common;

import java.time.LocalDateTime;

public interface AuditableEntity {
    void setOperator(String upn);
    String getOperator();
    void setTimestamp(LocalDateTime timestamp);
    LocalDateTime getTimestamp();
}
