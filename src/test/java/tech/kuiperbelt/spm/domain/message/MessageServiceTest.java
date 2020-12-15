package tech.kuiperbelt.spm.domain.message;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.idmapping.IdMappingService;
import tech.kuiperbelt.spm.domain.message.rule.Rule;
import tech.kuiperbelt.spm.domain.message.rule.RuleProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test
    public void bulkProcessEvent() {
        // Prepared test data
        String correlationId = RandomStringUtils.randomNumeric(10);
        Project project = Project.builder()
                .build();

        project.setId(RandomUtils.nextLong(0,1000));

        Phase phase = Phase.builder().build();
        phase.setId(RandomUtils.nextLong(0,1000));

        Event event1 = Event.builder()
                .correlationId(correlationId)
                .key(Event.PROJECT_ADDED)
                .source(project)
                .build();
        event1.setId(RandomUtils.nextLong(0,1000));

        Event event2 = Event.builder()
                .correlationId(correlationId)
                .key(Event.PROJECT_STARTED)
                .source(project)
                .build();
        event2.setId(RandomUtils.nextLong(0,1000));

        Event event3 = Event.builder()
                .correlationId(correlationId)
                .key(Event.PHASE_ADDED)
                .source(phase)
                .build();
        event3.setId(RandomUtils.nextLong(0,1000));


        Event.EventQueue eventQueue = new Event.EventQueue();
        eventQueue.add(event1);
        eventQueue.add(event2);
        eventQueue.add(event3);


        MessageService messageService = new MessageService();

        // Mock idMappingService
        IdMappingService idMappingService = mock(IdMappingService.class);

        when(idMappingService.findEntity(project.getId()))
                .thenReturn((Optional)Optional.of(project));
        when((idMappingService.findEntity(phase.getId())))
                .thenReturn((Optional)Optional.of(phase));

        messageService.setIdMappingService(idMappingService);

        // Mock RuleProvider
        RuleProvider ruleProvider = mock(RuleProvider.class);
        List<Rule> rules = Collections.singletonList(Rule.builder().build());
        when(ruleProvider.getAllRules()).thenReturn(rules);
        messageService.setRuleProvider(ruleProvider);

        MessageRepository messageRepository = mock(MessageRepository.class);

        messageService.setMessageRepository(messageRepository);

        messageService.bulkProcessEvent(eventQueue);

        verify(messageRepository, atLeast(3)).save(argThat(message ->
                message != null && message.getEvents() != null &&
                        message.getSource().equals(project.getId()) &&
                        message.getEvents().size() == 2 &&
                        message.getEvents().contains(event1.getId()) &&
                        message.getEvents().contains(event2.getId())));


        verify(messageRepository, atLeast(3)).save(argThat(message ->
                message != null && message.getEvents() != null &&
                        message.getSource().equals(phase.getId()) &&
                        message.getEvents().size() == 1 &&
                        message.getEvents().contains(event3.getId())));

    }


}