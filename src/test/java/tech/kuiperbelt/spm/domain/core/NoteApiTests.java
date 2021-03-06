package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = NoteApiTests.MOCK_UERR)
public class NoteApiTests extends ApiTest {
    public static final String MOCK_UERR = "sjdfs.ldjfds";

    @Sql({"/cleanup.sql"})
    @Test
    public void taskNoteForProject() throws Exception {
        String projectHref = testUtils.createRandomProject();
        String noteHref = testUtils.taskRandomNote(projectHref);

        mockMvc.perform(get(noteHref))
                .andExpect(jsonPath("$._links.project.href", equalTo(projectHref)));

        mockMvc.perform((get(projectHref + "/notes")))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(noteHref)));

        // Delete projects
        testUtils.delete(projectHref);

        mockMvc.perform(get(noteHref))
                .andExpect(status().isNotFound());
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void taskNoteForPhase() throws Exception {
        LocalDate current = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        String phaseHref = testUtils.appendRandomPhase(projectHref, current, current.plusDays(10));
        String noteHref = testUtils.taskRandomNote(phaseHref);

        mockMvc.perform(get(noteHref))
                .andExpect(jsonPath("$._links.phase.href", equalTo(phaseHref)));

        mockMvc.perform((get(phaseHref + "/notes")))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(noteHref)));

        // Delete projects
        testUtils.delete(phaseHref);

        mockMvc.perform(get(noteHref))
                .andExpect(status().isNotFound());
    }


    @Sql({"/cleanup.sql"})
    @Test
    public void taskNoteForWorkItem() throws Exception {
        LocalDate current = LocalDate.now();
        String workItemAHref = testUtils.createRandomWorkItem(current, current.plusDays(10));
        Note note = new Note().toBuilder()
                .content(RandomStringUtils.randomAlphanumeric(20))
                .build();
        String noteHref = mockMvc.perform(post(workItemAHref + "/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("location");
        assertThat(noteHref, notNullValue());
        mockMvc.perform(get(noteHref))
                .andExpect(jsonPath("$.content", equalTo(note.getContent())));

        mockMvc.perform((get(workItemAHref + "/notes")))
                .andExpect(jsonPath("$._embedded..self.href", hasItems(noteHref)));

        mockMvc.perform(get(noteHref))
                .andExpect(jsonPath("$._links.workItem.href", equalTo(workItemAHref)));

        // Delete projects
        testUtils.delete(workItemAHref);

        mockMvc.perform(get(noteHref))
                .andExpect(status().isNotFound());
    }

    @Sql({"/cleanup.sql"})
    @Test
    public void testEvent() throws Exception {
        LocalDate current = LocalDate.now();
        String projectHref = testUtils.createRandomProject();
        String phaseHref = testUtils.appendRandomPhase(projectHref, current, current.plusDays(10));
        String workItemHref = testUtils.createRandomWorkItem(current, current.plusDays(10));
        testUtils.cleanAll("/events");

        String noteHref = testUtils.taskRandomNote(projectHref);

        testUtils.verifyEvents(1, Event.PROJECT_NOTE_TAKEN);
        testUtils.verifyEventDetail(Event.PROJECT_NOTE_TAKEN, "project", projectHref,
                "The project","take note");

        testUtils.cleanAll("/events");
        testUtils.delete(noteHref);
        testUtils.verifyEvents(1, Event.NOTE_DELETED);

        testUtils.verifyEventDetail(Event.NOTE_DELETED, "note", null,
                "The note","is deleted");

        testUtils.cleanAll("/events");
        testUtils.taskRandomNote(phaseHref);
        testUtils.verifyEvents(1, Event.PHASE_NOTE_TAKEN);

        testUtils.verifyEventDetail(Event.PHASE_NOTE_TAKEN, "phase", phaseHref,
                "The project", "phase", "take note");

        testUtils.cleanAll("/events");
        testUtils.taskRandomNote(workItemHref);
        testUtils.verifyEvents(1, Event.ITEM_NOTE_TAKEN);

        testUtils.verifyEventDetail(Event.ITEM_NOTE_TAKEN, "workItem", workItemHref,
                "The workItem", "take note");
    }
}
