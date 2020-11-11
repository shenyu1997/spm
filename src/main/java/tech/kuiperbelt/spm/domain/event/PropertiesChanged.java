package tech.kuiperbelt.spm.domain.event;

import java.util.LinkedList;

public class PropertiesChanged extends LinkedList<PropertyChanged> {

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

        public PropertiesChanged build() {
            return propertiesChanged;
        }
    }
}
