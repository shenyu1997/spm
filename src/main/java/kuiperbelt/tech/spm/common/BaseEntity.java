package kuiperbelt.tech.spm.common;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(generator = "uuidLong")
    @GenericGenerator(name = "uuidLong", strategy = "kuiperbelt.tech.spm.common.MockGenerator")
    private Long id;
}
