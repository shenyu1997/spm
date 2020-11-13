package tech.kuiperbelt.spm.domain.sample;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Setter
@RestController
@RequestMapping("/sample-data")
public class SampleDataController {

    @Autowired
    private SampleDataService sampleDataService;


    @PostMapping("/actions/import")
    public void importSampleData() {
        sampleDataService.importSampleData();
    }

}
