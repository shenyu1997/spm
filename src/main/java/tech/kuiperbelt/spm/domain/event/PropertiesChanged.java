package tech.kuiperbelt.spm.domain.event;

import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

public class PropertiesChanged extends LinkedList<PropertyChanged> {

    public PropertyChanged getPropertyChanged(String propertyName) {
        Assert.hasText(propertyName, "Property name can not be empty");
        return this.stream().filter(propertyChanged -> propertyName.equals(propertyName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(propertyName + " is not fund in properties change list"));
    }

    public static PropertiesChangedBuilder builder() {
        return new PropertiesChangedBuilder();
    }

    public static class PropertiesChangedBuilder {
        private PropertiesChanged propertiesChanged = new PropertiesChanged();

        public PropertiesChangedBuilder append(String property, Object oldValue, Object newValue) {
            propertiesChanged.add(PropertyChanged.builder()
                    .property(property)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build());
            return this;
        }

        public PropertiesChangedBuilder append(PropertyChanged propertyChanged) {
            propertiesChanged.add(propertyChanged);
            return this;
        }

        public PropertiesChanged build() {
            return propertiesChanged;
        }
    }

    public static PropertiesChanged ofSingle(PropertyChanged propertyChanged) {
        Assert.notNull(propertyChanged, "propertyChanged can not be empty");
        PropertiesChanged propertiesChanged = new PropertiesChanged();
        propertiesChanged.add(propertyChanged);
        return propertiesChanged;
    }

    public static Optional<PropertiesChanged> of(Object oldValue, Object newValue, String ... properties) {
        PropertiesChangedBuilder builder = PropertiesChanged.builder();
        Stream.of(properties)
                .forEach(property -> builder.append(property, oldValue, newValue));
        PropertiesChanged propertiesChanged = builder.build();
        return propertiesChanged.isEmpty()?
                Optional.empty():
                Optional.of(propertiesChanged);
    }
}
