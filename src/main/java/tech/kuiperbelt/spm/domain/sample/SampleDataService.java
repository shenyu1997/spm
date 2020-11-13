package tech.kuiperbelt.spm.domain.sample;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Setter
@Service
public class SampleDataService {

    @Autowired
    private List<ImportCase> allImportCase;

    public void importSampleData() {
        for(ImportCase importCase: allImportCase) {
            log.info("Import sample data: {}", importCase.name());
            importCase.importDate();
            log.info("Done with importing sample data: {} ", importCase.name());
        }
    }
}
