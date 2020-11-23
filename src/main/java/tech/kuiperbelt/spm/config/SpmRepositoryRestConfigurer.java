package tech.kuiperbelt.spm.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import tech.kuiperbelt.spm.domain.core.support.BeforeCreateOrSaveBaseEntityValidator;

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

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);
    }
}
