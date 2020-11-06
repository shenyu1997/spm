package tech.kuiperbelt.spm.common;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.util.Comparator;
import java.util.Optional;

@Service
public class AuditService {

    @Autowired
    private EntityManager entityManager;

    public <T extends BaseEntity> Optional<T> getPreviousVersion(T current) {
        Assert.notNull(current, "current entity can not be null");
        AuditReader reader = AuditReaderFactory.get(entityManager.unwrap(Session.class));
        Optional<Number> previousVersion = reader.getRevisions(current.getClass(), current.getId())
                .stream()
                .sorted(Comparator.comparing(Number::longValue).reversed())
                .findFirst();
        if(!previousVersion.isPresent()) {
            return Optional.empty();
        }
        AuditQuery auditQuery = reader.createQuery().forEntitiesAtRevision(current.getClass(), previousVersion.get());
        T t = (T)auditQuery.getSingleResult();
        return Optional.of(t);
    }
}
