package tech.kuiperbelt.spm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ObjectMapperBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ObjectMapper) {
            ObjectMapper objectMapper = (ObjectMapper) bean;
            //TODO re-open it
            //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            log.info("Config objectMapper with JsonInclude.Include.NON_NULL");
        }
        return bean;
    }
}
