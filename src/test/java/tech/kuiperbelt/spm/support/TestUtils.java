package tech.kuiperbelt.spm.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tech.kuiperbelt.spm.domain.core.Note;
import tech.kuiperbelt.spm.domain.core.Phase;
import tech.kuiperbelt.spm.domain.core.Project;
import tech.kuiperbelt.spm.domain.core.WorkItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
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
        return mockMvc.perform(post("/phases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phase)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
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
        mockMvc.perform(post(href + "/actions/start"))
                .andExpect(status().isNoContent());
    }

    public void cancel(String href) throws Exception {
        mockMvc.perform(post(href + "/actions/cancel"))
                .andExpect(status().isNoContent());
    }

    public void done(String href) throws Exception {
        mockMvc.perform(post(href + "/actions/done"))
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
}
