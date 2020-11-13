package tech.kuiperbelt.spm.domain.sample.cases;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.function.Supplier;

@Setter
@Slf4j
public class ImportCaseSupport {

    @Autowired
    private EntityManager entityManager;


    public <T> T step(String name, Supplier<T> supplier) {
        try {
            log.info("Import sample data - step: {}", name);
            return supplier.get();
        } finally {
            reload();
        }
    }

    public void step(String name, Runnable runnable) {
        step(name, () -> {
            runnable.run();
            return null;
        });
    }

    private void reload() {
        entityManager.flush();
        entityManager.clear();
    }
}
