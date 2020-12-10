package tech.kuiperbelt.spm.domain.message.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.WorkItem;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.event.Event;

import java.util.Collection;
import java.util.Objects;

@AllArgsConstructor
@Builder
public class MessageDispatchRule {
    // ********* event party ********* //

    private boolean includeTriggerMan;

    private String eventKey;

    private Integer eventArgs;

    // ********* project party ********* //

    private Boolean isProjectOwner;

    private Boolean isProjectManager;

    private Boolean belongToProjectMember;

    private Boolean belongToProjectParticipant;

    // ********* work item party ********* //

    private Boolean isWorkItemOwner;

    private Boolean isWorkItemAssignee;

    private Boolean isMilestone;


    public boolean evaluate(@NonNull Event event, @NonNull String upn, WorkItem workItem, Phase phase, Project project) {

        // ********* event party ********* //
        if(check(includeTriggerMan, Objects.equals(event.getTriggeredMan(), upn))) {
            return false;
        }

        if(eventKey != null && !matchKey(event, eventKey)) {
            return false;
        }

        if(eventArgs != null && !matchArgs(event, upn)) {
            return false;
        }

        // ********* project party ********* //
        if(project != null) {
            // match upn
            if(check(isProjectOwner, Objects.equals(upn, project.getOwner()))) {
                return false;
            }
            if(check(isProjectManager, Objects.equals(upn,project.getManager()))) {
                return false;
            }
            if(check(belongToProjectMember, include(upn, project.getMembers()))){
                return false;
            }
            if(check(belongToProjectParticipant, include(upn, project.getParticipants()))){
                return false;
            }
        }

        // ********* work item party ********* //
        if(workItem != null) {
            if(check(isWorkItemOwner, Objects.equals(upn, workItem.getOwner()))) {
                return false;
            }

            if(check(isWorkItemAssignee, Objects.equals(upn, workItem.getAssignee()))) {
                return false;
            }

            return !check(isMilestone, workItem.isMilestone());
        }
        return true;
    }

    private boolean check(Boolean condition, boolean actualValue) {
        return condition != null && !Objects.equals(condition, actualValue);
    }

    private boolean matchArgs(Event event, String upn) {
        Assert.isTrue(eventArgs < event.getArgs().length, "eventArgs must less then event arg length");
        Object arg = event.getArgs()[this.eventArgs];
        if(arg instanceof Collection<?>) {
            return ((Collection<?>) arg).contains(upn);
        } else {
            return Objects.equals(arg, upn);
        }
    }

    /**
     *
     * @param event like event: event.project.member.add
     * @param expect event.project.member.*
     * @return isMatch
     */
    private boolean matchKey(Event event, String expect) {
        String actual = event.getKey();
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

    private boolean include(String upn, Collection<String> testCollection) {
        return !CollectionUtils.isEmpty(testCollection) && testCollection.contains(upn);
    }

}
