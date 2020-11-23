package tech.kuiperbelt.spm.domain.core.support;

import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Component
@RequestScope
public class UserContext {
    private boolean init;
    private String upn;
    private String correlationId;

    public static UserContext of(UserContext userContext) {
        return UserContext.builder()
                .init(userContext.init)
                .upn(userContext.upn)
                .correlationId(userContext.correlationId)
                .build();
    }
}
