package tech.kuiperbelt.spm.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import tech.kuiperbelt.spm.common.BeforeCreateOrSaveBaseEntityValidator;

@Setter
@Configuration
public class SpmRepositoryRestConfigurer implements RepositoryRestConfigurer {

    @Autowired
    private BeforeCreateOrSaveBaseEntityValidator beforeCreateOrSaveBaseEntityValidator;

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        validatingListener.addValidator("beforeCreate", beforeCreateOrSaveBaseEntityValidator);
        validatingListener.addValidator("beforeSave", beforeCreateOrSaveBaseEntityValidator);
    }
}
