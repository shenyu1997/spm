package kuiperbelt.tech.spm.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Setter
@Component
@RequestScope
public class UserContext {
    private boolean init;
    private String upn;
    private String correlationId;
}
