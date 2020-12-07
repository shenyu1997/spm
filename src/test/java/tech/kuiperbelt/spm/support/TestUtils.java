package tech.kuiperbelt.spm.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.IsIterableContaining;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tech.kuiperbelt.spm.domain.core.*;
import tech.kuiperbelt.spm.domain.core.support.ExecutableEntity;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AllArgsConstructor
public class TestUtils {
    public static final String LOCATION = "location";

    private final  MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public String createRandomProject() throws Exception {
        Project newProject = new Project().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .build();

        return mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProject)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public String insertRandomPhase(String newProjectHref, int sequence, LocalDate plannedEndDate) throws Exception {
        return insertRandomPhase(newProjectHref, sequence, null, plannedEndDate);
    }

    public String insertRandomPhase(String newProjectHref, int sequence, LocalDate plannedStartDate, LocalDate plannedEndDate) throws Exception {
        Phase phase = new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(6))
                .plannedStartDate(plannedStartDate)
                .plannedEndDate(plannedEndDate)
                .seq(sequence)
                .build();
        Map<String, String> phaseStrMap = BeanUtils.describe(phase);
        phaseStrMap.put(Phase.Fields.project, newProjectHref);
        phaseStrMap = filterByFields(phaseStrMap,
                Phase.Fields.name,
                Phase.Fields.plannedStartDate,
                Phase.Fields.plannedEndDate,
                Phase.Fields.seq,
                Phase.Fields.project);

        return mockMvc.perform(post("/phases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phaseStrMap)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public String insertRandomPhaseFromProject(String newProjectHref, int sequence, LocalDate plannedStartDate, LocalDate plannedEndDate) throws Exception {
        Phase phase = new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(6))
                .plannedStartDate(plannedStartDate)
                .plannedEndDate(plannedEndDate)
                .seq(sequence)
                .build();

        return mockMvc.perform(post(newProjectHref + "/phases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public String appendRandomPhase(String newProjectHref,	 LocalDate plannedEndDate) throws Exception {
        return appendRandomPhase(newProjectHref, null, plannedEndDate);
    }

    public String appendRandomPhase(String newProjectHref,
                                     LocalDate plannedStartDate,
                                     LocalDate plannedEndDate) throws Exception {
        Map<String, String> phase = BeanUtils.describe(new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(6))
                .plannedEndDate(plannedEndDate)
                .plannedStartDate(plannedStartDate)
                .build());
        phase.put(Phase.Fields.project, newProjectHref);
        phase = filterByFields(phase,
                Phase.Fields.name,
                Phase.Fields.plannedStartDate,
                Phase.Fields.plannedEndDate,
                Phase.Fields.project);

        return mockMvc.perform(post("/phases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    private Map<String, String> filterByFields(Map<String, String> original, String... fields) {
        HashSet<String> filedSet = new HashSet<>(Arrays.asList(fields));
        Map<String, String> result = new HashMap<>();
        for(String name: original.keySet()) {
            if(filedSet.contains(name)) {
                result.put(name, original.get(name));
            }
        }
        return result;
    }

    public String appendRandomPhaseFromProject(String newProjectHref,	 LocalDate plannedEndDate) throws Exception {
        return appendRandomPhaseFromProject(newProjectHref, null, plannedEndDate);
    }

    public String appendRandomPhaseFromProject(String newProjectHref,
                                    LocalDate plannedStartDate,
                                    LocalDate plannedEndDate) throws Exception {
        Phase phase = new Phase().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(6))
                .plannedEndDate(plannedEndDate)
                .plannedStartDate(plannedStartDate)
                .build();

        return mockMvc.perform(post(newProjectHref + "/phases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public void start(String href) throws Exception {
        mockMvc.perform(post(href + ":start"))
                .andExpect(status().isNoContent());
    }

    public void cancel(String href) throws Exception {
        mockMvc.perform(post(href + ":cancel"))
                .andExpect(status().isNoContent());
    }

    public void done(String href) throws Exception {
        mockMvc.perform(post(href + ":done"))
                .andExpect(status().isNoContent());
    }

    public void delete(String href) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(href))
                .andExpect(status().isNoContent());
    }

    public String createRandomPhaseWorkItem(String phaseHref, LocalDate plannedStartDate, LocalDate deadLine) throws Exception {
        WorkItem workItem = new WorkItem().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .plannedStartDate(plannedStartDate)
                .deadLine(deadLine)
                .build();
        return mockMvc.perform(post(phaseHref + "/work-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public String createRandomProjectWorkItem(String phaseHref, LocalDate plannedStartDate, LocalDate deadLine) throws Exception {
        WorkItem workItem = new WorkItem().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .plannedStartDate(plannedStartDate)
                .deadLine(deadLine)
                .build();
        return mockMvc.perform(post(phaseHref + "/direct-work-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }



    public String createRandomWorkItem(LocalDate plannedStartDate, LocalDate deadLine) throws Exception {
        WorkItem workItem = new WorkItem().toBuilder()
                .name(RandomStringUtils.randomAlphanumeric(10))
                .plannedStartDate(plannedStartDate)
                .deadLine(deadLine)
                .build();
        return mockMvc.perform(post("/work-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public String taskRandomNote(String workItemHref) throws Exception {
        Note note = new Note().toBuilder()
                .content(RandomStringUtils.randomAlphanumeric(20))
                .build();
        return mockMvc.perform(post(workItemHref + "/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
    }

    public void patchUpdate(String href, Object workItem) throws Exception {
        mockMvc.perform(patch(href)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workItem)))
                .andExpect(status().isNoContent());
    }

    public String getBody(String href) throws Exception {
        return mockMvc.perform(get(href))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    public void cleanAll(String href) throws Exception {
        String contentAsString = mockMvc.perform(get(href))
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<String> links =  JsonPath.read(contentAsString, "$._embedded..self.href");
        for(String link: links) {
            delete(link);
        }
    }

    public void verifyEvents(int eventTotalCount, String ... events) throws Exception {
        String eventsHref = "/events";
        String body = getBody(eventsHref);
        mockMvc.perform(get(eventsHref))
                .andExpect(jsonPath("$._embedded.events..key", hasItems(events)))
                .andExpect(jsonPath("$._embedded.events.length()", equalTo(eventTotalCount)));
    }

    public void verifyEventDetail(String eventKey, String sourceType, String sourceHref, String ... segments) throws Exception {
        String eventsHref = "/events";
        String body = mockMvc.perform(get(eventsHref).locale(Locale.US))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String eventPath = "$._embedded.events[?(@.key=='" + eventKey + "')]";
        List<String> links = JsonPath.read(body, eventPath + "._links." + sourceType + ".href");
        assertThat(links, hasItems(sourceHref));

        List<String> details = JsonPath.read(body, eventPath + ".detail");
        assertThat(details.size(), equalTo(1));
        for(String segment: segments) {
            assertTrue(details.get(0).contains(segment),"Should contain " + segment + ", whole detail is " + details.get(0));
        }
    }

    public void verifyStatusWithActions(String href, RunningStatus status, ExecutableEntity.Action ... actions) throws Exception {
        Object[] actionNames = Stream.of(actions)
                .map(Enum::name)
                .collect(Collectors.toList())
                .toArray();
        mockMvc.perform(get(href))
                .andExpect(jsonPath("$.status", equalTo(status.name())))
                .andExpect(jsonPath("$.actions", IsIterableContaining.hasItems(actionNames)));
    }

    public void verifyStatus(String href, RunningStatus status) throws Exception {
        mockMvc.perform(get(href))
                .andExpect(jsonPath("$.status", equalTo(status.name())));
    }


    public void verifyStatusWithoutActions(String href, RunningStatus status, ExecutableEntity.Action ... actions) throws Exception {
        Object[] actionNames = Stream.of(actions)
                .map(Enum::name)
                .collect(Collectors.toList())
                .toArray();
        mockMvc.perform(get(href))
                .andExpect(jsonPath("$.status", equalTo(status.name())))
                .andExpect(jsonPath("$.actions", not(hasItems(actionNames))));
    }

    public void verifyIsCanceled(String href) throws Exception {
        mockMvc.perform(get(href))
                .andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
                .andExpect(jsonPath("$.cancelled", equalTo(true)));
    }
}
