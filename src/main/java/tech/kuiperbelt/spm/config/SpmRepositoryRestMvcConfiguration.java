package tech.kuiperbelt.spm.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.support.JpaHelper;
import org.springframework.web.context.request.WebRequestInterceptor;
import tech.kuiperbelt.spm.domain.core.NoteController;
import tech.kuiperbelt.spm.domain.core.PhaseController;
import tech.kuiperbelt.spm.domain.core.ProjectController;
import tech.kuiperbelt.spm.domain.core.WorkItemController;
import tech.kuiperbelt.spm.domain.core.event.EventController;
import tech.kuiperbelt.spm.domain.core.event.EventService;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;
import tech.kuiperbelt.spm.domain.core.support.WebTransactionInterceptor;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SpmRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {


    @Autowired
    private WebTransactionInterceptor webTransactionInterceptor;


    public SpmRepositoryRestMvcConfiguration(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }

    @Bean
    @Override
    public JpaHelper jpaHelper() {
        JpaHelper jpaHelper = super.jpaHelper();
        if(jpaHelper == null) {
            return null;
        }
        return new JpaHelper() {
            @Override
            public List<WebRequestInterceptor> getInterceptors() {
                List<WebRequestInterceptor> requestInterceptors = new ArrayList<>(super.getInterceptors());
                requestInterceptors.add(webTransactionInterceptor);
                return requestInterceptors;
            }
        };
    }

    @Bean
    public ProjectController.ProjectRepresentationModelProcessor projectRepresentationModelProcessor() {
        return new ProjectController.ProjectRepresentationModelProcessor();
    }

    @Bean
    public PhaseController.PhaseRepresentationModelProcessor phaseRepresentationModelProcessor() {
        return new PhaseController.PhaseRepresentationModelProcessor();
    }

    @Bean
    public WorkItemController.WorkItemRepresentationModelProcessor workItemRepresentationModelProcessor() {
        return new WorkItemController.WorkItemRepresentationModelProcessor();
    }

    @Bean
    public NoteController.NoteRepresentationModelProcessor noteRepresentationModelProcessor(IdMappingService idMappingService) {
        return new NoteController.NoteRepresentationModelProcessor(idMappingService);
    }

    @Bean
    public EventController.EventRepresentationModelProcessor eventRepresentationModelProcessor(EventService eventService,
                                                                                               IdMappingService idMappingService) {
        return new EventController.EventRepresentationModelProcessor(eventService, idMappingService);
    }

}
