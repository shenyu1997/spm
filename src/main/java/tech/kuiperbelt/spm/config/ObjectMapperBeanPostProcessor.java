package tech.kuiperbelt.spm.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Slf4j
@Component
public class ObjectMapperBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ObjectMapper) {
            ObjectMapper objectMapper = (ObjectMapper) bean;
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            log.info("Config objectMapper with JsonInclude.Include.NON_NULL");
        }
        return bean;
    }
}
