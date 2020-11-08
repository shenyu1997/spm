package tech.kuiperbelt.spm.domain.event;

import tech.kuiperbelt.spm.domain.message.RoutingKey;

public enum EventType implements RoutingKey {
    PROJECT_CREATED("event.project.created"),
    PROJECT_CANCELED("event.project.canceled"),
    EVENT_PROJECT_REMOVED("event.project.removed"),

    PROJECT_OWNER_CHANGED("event.project.owner.changed"),
    PROJECT_MANAGER_CHANGED("event.project.manager.changed"),
    PROJECT_MEMBER_ADDED("event.project.member.added"),
    PROJECT_MEMBER_REMOVED("event.project.member.removed"),

    PROJECT_PROPERTIES_NAME_CHANGE("event.project.properties.name.change"),

    SYSTEM_BULK_END("event.system.bulk.end");

    private final String key;

    EventType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
