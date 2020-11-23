package tech.kuiperbelt.spm.domain.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
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

    public static PropertyChanged of(Map<Object, Object> map) {
        return PropertyChanged.builder()
                .property((String) map.get(Fields.property))
                .oldValue(map.get(Fields.oldValue))
                .newValue(map.get(Fields.newValue))
                .build();
    }

    public <T, R> PropertyChanged map(Class<T> clazz, Function<T,R> mapFunction) {
        PropertyChangedBuilder mapBuilder = PropertyChanged.builder().property(getProperty());
        if(getOldValue() != null) {
            mapBuilder.oldValue(mapFunction.apply(clazz.cast(getOldValue())));
        }
        if(getNewValue() != null) {
            mapBuilder.newValue(mapFunction.apply(clazz.cast(getNewValue())));
        }
        return mapBuilder.build();
    }

    public static class JsonSerializer implements com.google.gson.JsonSerializer<PropertyChanged> {

        @Override
        public JsonElement serialize(PropertyChanged propertyChanged, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty(Fields.property, propertyChanged.getProperty());
            if(propertyChanged.getOldValue() != null) {
                jObject.addProperty(Fields.oldValue, propertyChanged.getOldValue().toString());
            }

            if(propertyChanged.getNewValue() != null) {
                jObject.addProperty(Fields.newValue, propertyChanged.getNewValue().toString());
            }
            return jObject;
        }
    }
}
