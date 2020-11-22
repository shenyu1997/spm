package tech.kuiperbelt.spm.domain.event;

import lombok.*;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.Objects;
import java.util.Optional;

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

    @SneakyThrows
    public static <T>  Optional<PropertyChanged> of(T oldBean, T newBean, String property) {
        if(isChange(oldBean, newBean, property)) {
            return Optional.of(PropertyChanged.builder()
                    .property(property)
                    .oldValue(PropertyUtils.getSimpleProperty(oldBean, property))
                    .newValue(PropertyUtils.getSimpleProperty(newBean, property))
                    .build());
        } else {
            return Optional.empty();
        }
    }
}
