package tech.kuiperbelt.spm.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import tech.kuiperbelt.spm.domain.idmapping.IdMappingListener;

import javax.persistence.*;
import java.util.Objects;

@EntityListeners(IdMappingListener.class)
@MappedSuperclass
public class BaseEntity {
    @Getter
    @Setter
    @Id
    @GeneratedValue(generator = "uuidLong")
    @GenericGenerator(name = "uuidLong", strategy = "tech.kuiperbelt.spm.common.MockGenerator")
    private Long id;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }


    @JsonIgnore
    @Transient
    private Integer hash;

    @Override
    public final int hashCode() {
        if(this.hash == null) {
            if(this.id == null) {
                hash = System.identityHashCode(this);
            } else {
                hash = Objects.hash(id);
            }
        }
        return hash;
    }
}
