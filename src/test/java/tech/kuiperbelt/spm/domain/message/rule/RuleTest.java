package tech.kuiperbelt.spm.domain.message.rule;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kuiperbelt.spm.domain.core.event.Event;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest {

    private String upn;

    @BeforeEach
    void before() {
        upn = RandomStringUtils.randomAlphanumeric(10);
    }

    @Test
    void evaluateKey() {
        Rule rule1 = Rule.builder()
                .eventKey("event.test.*.*")
                .build();

        Event event1 = Event.builder()
                .key("event.test.a.b")
                .build();

        assertTrue(rule1.evaluate(event1, upn, null, null, null));

        //
        Rule rule2 = Rule.builder()
                .eventKey("event.test.*.*")
                .build();

        Event event2 = Event.builder()
                .key("event.test.a.b.c")
                .build();

        assertFalse(rule2.evaluate(event2, upn, null, null, null));

        //
        Rule rule3 = Rule.builder()
                .eventKey("event.test.*.*")
                .build();

        Event event3 = Event.builder()
                .key("event.c.a.b")
                .build();

        assertFalse(rule3.evaluate(event3, upn, null, null, null));

        //
        Rule rule4 = Rule.builder()
                .eventKey("event.test.a.c")
                .build();

        Event event4 = Event.builder()
                .key("event.test.a.c")
                .build();

        assertTrue(rule4.evaluate(event4, upn, null, null, null));

        //
        Rule rule5 = Rule.builder()
                .eventKey("event.test.a.c")
                .build();

        Event event5 = Event.builder()
                .key("event.test.s.c")
                .build();

        assertFalse(rule5.evaluate(event5, upn, null, null, null));

    }

    @Test
    public void evaluateTriggerMan() {
        // default
        Rule rule = Rule.builder().build();
        Event event = Event.builder().triggeredMan(RandomStringUtils.randomAlphanumeric(6)).build();
        assertTrue(rule.evaluate(event, upn, null, null, null));

        // triggerMan need match, suppose true
        Rule rule2 = Rule.builder().includeTriggerMan(true).build();
        Event event2 = Event.builder().triggeredMan(upn).build();
        assertTrue(rule2.evaluate(event2, upn, null, null, null));

        // triggerMan need match, suppose false
        Rule rule3 = Rule.builder().includeTriggerMan(true).build();
        Event event3 = Event.builder().triggeredMan(RandomStringUtils.randomAlphanumeric(10)).build();
        assertFalse(rule3.evaluate(event3, upn, null, null, null));


    }
}