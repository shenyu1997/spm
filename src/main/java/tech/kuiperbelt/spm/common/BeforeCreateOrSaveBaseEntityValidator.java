package tech.kuiperbelt.spm.common;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Setter
@Component
public class BeforeCreateOrSaveBaseEntityValidator implements Validator {

    @Autowired
    private Validator validator;

    @Override
    public boolean supports(Class<?> aClass) {
        return BaseEntity.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        validator.validate(o, errors);
    }
}
