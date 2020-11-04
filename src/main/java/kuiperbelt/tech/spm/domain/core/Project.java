package kuiperbelt.tech.spm.domain.core;

import kuiperbelt.tech.spm.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {
    public final static String ENTITY_TYPE = "Project";

    private String name;

    private String description;

    private String owner;

    private String manager;

    @ElementCollection
    private List<String> members;

    @Enumerated(EnumType.STRING)
    private RunningStatus status;

    public Set<String> getParticipants() {
        Set result = new HashSet();
        if(!CollectionUtils.isEmpty(members)) {
            result.addAll(members);
        }
        result.add(owner);
        return result;
    }
}
