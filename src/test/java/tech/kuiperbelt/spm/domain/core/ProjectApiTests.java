package tech.kuiperbelt.spm.domain.core;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.support.ExecutableEntity;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = ProjectApiTests.MOCK_UERR)
class ProjectApiTests extends ApiTest {

	public static final String MOCK_UERR = "sjdfs.ldjfds";
	public static final String LOCATION = "location";

	@Sql({"/cleanup.sql"})
	@Test
	public void getProjects() throws Exception {
		mockMvc.perform(get("/projects"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.number", equalTo(0)));
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void createProject() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = mockMvc.perform(post("/projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newProject)))
				.andExpect(status().isCreated())
				.andExpect(header().exists(LOCATION))
				.andReturn()
				.getResponse()
				.getHeader(LOCATION);

		assertThat(newProjectHref, notNullValue());
		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", equalTo(newProject.getName())))
				.andExpect(jsonPath("$.owner", equalTo(MOCK_UERR)))
				.andExpect(jsonPath("$.manager", equalTo(MOCK_UERR)))
				.andExpect(jsonPath("$._links['self'].href", equalTo(newProjectHref)));
		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.INIT,
				ExecutableEntity.Action.start, ExecutableEntity.Action.delete, ExecutableEntity.Action.cancel);
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void getMyProjects() {
		//TODO
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void cancelInitProjectWithNoPhase() throws Exception {

		String newProjectHref = testUtils.createRandomProject();
		testUtils.cancel(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)));
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void cancelInitProjectWithPhases() throws Exception {

		String newProjectHref = testUtils.createRandomProject();
		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		testUtils.cancel(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)));

		mockMvc.perform(get(fistPhaseHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)));

		mockMvc.perform(get(secondPhaseHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)));
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void deleteInitProjectWithNoPhase() throws Exception {
		String newProjectHref = testUtils.createRandomProject();
		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void deleteInitProjectWithPhases() throws Exception {
		String newProjectHref = testUtils.createRandomProject();

		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(fistPhaseHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(secondPhaseHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void startProjectWithNoPhase() throws Exception {
		String newProjectHref = testUtils.createRandomProject();
		testUtils.start(newProjectHref);

		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.RUNNING,
				ExecutableEntity.Action.cancel,
				ExecutableEntity.Action.done);
		testUtils.verifyStatusWithoutActions(newProjectHref, RunningStatus.RUNNING,
				ExecutableEntity.Action.start,
				ExecutableEntity.Action.delete);

	}

	@Sql({"/cleanup.sql"})
	@Test
	public void startProjectWithPhases() throws Exception {
		String newProjectHref = testUtils.createRandomProject();

		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		testUtils.start(newProjectHref);
		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.RUNNING,
				ExecutableEntity.Action.cancel);
		testUtils.verifyStatusWithoutActions(newProjectHref, RunningStatus.RUNNING,
				ExecutableEntity.Action.start,
				ExecutableEntity.Action.delete,
				ExecutableEntity.Action.done);

		testUtils.verifyStatus(fistPhaseHref, RunningStatus.RUNNING);

		testUtils.verifyStatus(secondPhaseHref, RunningStatus.INIT);

	}

	@Sql({"/cleanup.sql"})
	@Test
	public void doneAndDeleteProjectWithNoPhase() throws Exception {
		String newProjectHref = testUtils.createRandomProject();
		testUtils.start(newProjectHref);
		testUtils.done(newProjectHref);

		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.STOP, ExecutableEntity.Action.delete);
		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void doneAndDeleteProjectWithPhases() throws Exception {
		String newProjectHref = testUtils.createRandomProject();

		// add a phase
		String newPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));

		// start project, and first phase will be sets Running automatically
		testUtils.start(newProjectHref);

		testUtils.done(newPhaseHref);

		testUtils.done(newProjectHref);

		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.STOP, ExecutableEntity.Action.delete);
		testUtils.verifyStatusWithoutActions(newProjectHref, RunningStatus.STOP,
				ExecutableEntity.Action.start,
				ExecutableEntity.Action.cancel,
				ExecutableEntity.Action.done);

		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(newPhaseHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void cancelAndRemoveProjectWithNoPhase() throws Exception {
		String newProjectHref = testUtils.createRandomProject();
		testUtils.start(newProjectHref);
		testUtils.cancel(newProjectHref);

		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.STOP, ExecutableEntity.Action.delete);

		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void cancelAndDeleteProjectWithPhases() throws Exception {
		String newProjectHref = testUtils.createRandomProject();

		// add a phase
		String newPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));

		// start project, and first phase will be sets Running automatically
		testUtils.start(newProjectHref);

		testUtils.cancel(newProjectHref);

		testUtils.verifyStatusWithActions(newProjectHref, RunningStatus.STOP, ExecutableEntity.Action.delete);
		testUtils.verifyIsCanceled(newProjectHref);

		testUtils.verifyStatusWithActions(newPhaseHref, RunningStatus.STOP, ExecutableEntity.Action.delete);
		testUtils.verifyIsCanceled(newPhaseHref);

		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(newPhaseHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void testHappyEvent() throws Exception {
		String projectHref = testUtils.createRandomProject();

		testUtils.verifyEvents(3,
				Event.PROJECT_ADDED,
				Event.PROJECT_OWNER_CHANGED,
				Event.PROJECT_MANAGER_CHANGED);

		testUtils.start(projectHref);
		testUtils.verifyEvents(4, Event.PROJECT_STARTED);
		testUtils.verifyEventDetail(Event.PROJECT_STARTED, "project", projectHref,
				"The project",
				"is started");

		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.name,RandomStringUtils.randomAlphanumeric(10)));
		testUtils.verifyEvents(5, Event.PROJECT_PROPERTIES_CHANGED);

		testUtils.verifyEventDetail(Event.PROJECT_PROPERTIES_CHANGED, "project", projectHref,
				"properties changed",
				"the name");

		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.owner,RandomStringUtils.randomAlphanumeric(10)));
		testUtils.verifyEvents(6, Event.PROJECT_OWNER_CHANGED);
		testUtils.verifyEventDetail(Event.PROJECT_OWNER_CHANGED, "project", projectHref,
				"project", "owner changed",
				"the owner");

		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.manager,RandomStringUtils.randomAlphanumeric(10)));
		testUtils.verifyEvents(7, Event.PROJECT_MANAGER_CHANGED);

		testUtils.verifyEventDetail(Event.PROJECT_MANAGER_CHANGED, "project", projectHref,
				"project", "manager changed",
				"the manager");

		testUtils.cleanAll("/events");
		String memberA = RandomStringUtils.randomAlphanumeric(10);
		String memberB = RandomStringUtils.randomAlphanumeric(10);
		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.members, Lists.newArrayList(memberA, memberB)));
		testUtils.verifyEvents(1, Event.PROJECT_MEMBER_ADDED);

		testUtils.verifyEventDetail(Event.PROJECT_MEMBER_ADDED, "project", projectHref,
				"join the project");

		testUtils.cleanAll("/events");
		String memberC = RandomStringUtils.randomAlphanumeric(10);
		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.members, Lists.newArrayList(memberA, memberC)));
		testUtils.verifyEvents(2,
				Event.PROJECT_MEMBER_ADDED,
				Event.PROJECT_MEMBER_DELETED);

		testUtils.verifyEventDetail(Event.PROJECT_MEMBER_ADDED, "project", projectHref,
				"join the project");

		testUtils.verifyEventDetail(Event.PROJECT_MEMBER_DELETED, "project", projectHref,
				"leave from the project");

		testUtils.cleanAll("/events");
		testUtils.done(projectHref);
		testUtils.verifyEvents(1, Event.PROJECT_DONE);

		testUtils.verifyEventDetail(Event.PROJECT_DONE, "project", projectHref,
				"The project", "is done");

		testUtils.delete(projectHref);
		testUtils.verifyEvents(2, Event.PROJECT_DELETED);

		testUtils.verifyEventDetail(Event.PROJECT_DELETED, "project", null,
				"The project", "is deleted");
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void testCancelEvent() throws Exception {
		String projectHref = testUtils.createRandomProject();

		testUtils.cleanAll("/events");

		testUtils.cancel(projectHref);
		testUtils.verifyEvents(1,
				Event.PROJECT_CANCELED);

		testUtils.verifyEventDetail(Event.PROJECT_CANCELED, "project", null,
				"The project", "is canceled");
	}
}
