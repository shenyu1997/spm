package tech.kuiperbelt.spm.domain.event;

import lombok.*;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyChanged {
    private String property;
    private Object oldValue;
    private Object newValue;

    @SneakyThrows
    public static <T>  boolean isChange(T oldBean, T newBean, String property) {
        Object oldValue = PropertyUtils.getSimpleProperty(oldBean, property);
        Object newValue = PropertyUtils.getSimpleProperty(newBean, property);
        return !Objects.equals(oldValue, newValue);
    }
}
