package tech.kuiperbelt.spm.domain.message.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.event.Event;

import java.util.Collection;
import java.util.Objects;

@AllArgsConstructor
@Builder
public class ProjectEventReceiveRule {
    private String key;

    private boolean trigger; //TODO

    private Boolean owner;

    private Boolean manager;

    private Boolean member;

    private Boolean participant;

    private Integer args;

    public boolean evaluate(Event event, String upn, BaseEntity baseEntity) {
        if(baseEntity instanceof Project) {
            return evaluate(event, upn, (Project) baseEntity);
        }
        return false;
    }

    public boolean evaluate(Event event, String upn, Project project) {
        if(key != null && !matchKey(event.getKey(), key)) {
            return false;
        }

        // match upn
        if(owner != null && owner && Objects.equals(upn,project.getOwner())) {
            return true;
        }
        if(manager != null && manager && Objects.equals(upn,project.getManager())) {
            return true;
        }
        if(member != null && member &&
                (project.getMembers() != null &&
                project.getMembers().contains(upn))) {
            return true;
        }
        if(participant != null && participant && project.getParticipants().contains(upn)) {
            return true;
        }

        if(args != null && matchArgs(event, upn)) {
            return true;
        }
        return false;
    }

    private boolean matchArgs(Event event, String upn) {
        Object arg = event.getArgs()[this.args];
        if(arg instanceof Collection) {
            return ((Collection) arg).contains(upn);
        } else {
            return Objects.equals(arg, upn);
        }
    }

    /**
     *
     * @param actual event.project.member.add
     * @param expect event.project.member.*
     * @return
     */
    private boolean matchKey(String actual, String expect) {
        String[] actualParts = actual.split("\\.");
        String[] expectParts = expect.split("\\.");
        if(actualParts.length != expectParts.length) {
            return false;
        }
        for(int i=0; i<expectParts.length; i++) {
            if(!("*".equals(expectParts[i]) || Objects.equals(actualParts[i], expectParts[i]))) {
                return false;
            }
        }
        return true;
    }
}
