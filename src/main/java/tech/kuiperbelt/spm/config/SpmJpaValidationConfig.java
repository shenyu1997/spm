package tech.kuiperbelt.spm.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.Validator;

@Configuration
public class SpmJpaValidationConfig implements BeanFactoryAware {
    private Validator validator;

    // 将Spring Validator 配置到JPA中， 这样可以在自定义的Validator中用 Autowired 注入Spring Bean
    @Bean
    @Lazy
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            if(validator != null) {
                hibernateProperties.put("javax.persistence.validation.factory", validator);
            }
        };
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        validator = beanFactory.getBean(Validator.class);
    }
}
