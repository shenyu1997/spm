package tech.kuiperbelt.spm.domain.core.event;

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


@AllArgsConstructor
@FieldNameConstants
@Builder
public class PropertyChanged {

    @Getter
    private String property;
    private Object oldValue;
    private Object newValue;

    public Optional<Object> getOldValue() {
        return Optional.ofNullable(oldValue);
    }

    public Optional<Object> getNewValue() {
        return Optional.ofNullable(newValue);
    }

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
        getOldValue().ifPresent(oldValue ->
                mapBuilder.oldValue(mapFunction.apply(clazz.cast(oldValue))));
        getNewValue().ifPresent(newValue ->
                mapBuilder.newValue(mapFunction.apply(clazz.cast(newValue))));
        return mapBuilder.build();
    }

    public static class JsonSerializer implements com.google.gson.JsonSerializer<PropertyChanged> {

        @Override
        public JsonElement serialize(PropertyChanged propertyChanged, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty(Fields.property, propertyChanged.getProperty());
            propertyChanged.getOldValue().ifPresent(oldValue ->
                    jObject.addProperty(Fields.oldValue, oldValue.toString()));

            propertyChanged.getNewValue().ifPresent(newValue ->
                    jObject.addProperty(Fields.newValue, newValue.toString()));

            return jObject;
        }
    }
}
