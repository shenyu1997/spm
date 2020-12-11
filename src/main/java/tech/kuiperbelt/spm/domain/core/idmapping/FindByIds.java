package tech.kuiperbelt.spm.domain.core.idmapping;

import java.util.List;

public interface FindByIds<T, ID> {
    List<T> findByIds(List<ID> ids);
}
