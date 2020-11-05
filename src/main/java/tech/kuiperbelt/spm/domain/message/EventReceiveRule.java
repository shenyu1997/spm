package tech.kuiperbelt.spm.domain.message;

import tech.kuiperbelt.spm.common.BaseEntity;
import tech.kuiperbelt.spm.domain.event.Event;
import tech.kuiperbelt.spm.domain.core.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Objects;

@AllArgsConstructor
@Builder
public class EventReceiveRule {

    private Event.Type type;

    private String subType;

    private Boolean owner;

    private Boolean manager;

    private Boolean member;

    private Boolean participant;
    public boolean evaluate(Event event, String upn, BaseEntity baseEntity) {
        if(baseEntity instanceof Project) {
            return evaluate(event, upn, (Project) baseEntity);
        }
        return false;
    }

    public boolean evaluate(Event event, String upn, Project project) {
        if(type != null && type != event.getType()) {
            return false;
        }
        if(subType != null && subType != event.getSubType()) {
            return false;
        }
        if(owner != null && owner && !Objects.equals(upn,project.getOwner())) {
            return false;
        }

        if(manager != null && manager && !Objects.equals(upn,project.getManager())) {
            return false;
        }
        if(member != null && member &&
                (project.getMembers() == null ||
                !project.getMembers().contains(upn))) {
            return false;
        }
        if(participant != null && participant && !project.getParticipants().contains(upn)) {
            return false;
        }
        return true;
    }
}
