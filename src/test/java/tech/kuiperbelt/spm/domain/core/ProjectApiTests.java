package tech.kuiperbelt.spm.domain.core;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tech.kuiperbelt.spm.domain.core.event.Event;
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
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(true)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)))
				.andExpect(jsonPath("$._links['self'].href", equalTo(newProjectHref)));
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

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(false)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeDone", equalTo(true)));
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void startProjectWithPhases() throws Exception {
		String newProjectHref = testUtils.createRandomProject();

		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		testUtils.start(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(false)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));  //because there are Non STOP phase

		mockMvc.perform(get(fistPhaseHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())));
		mockMvc.perform(get(secondPhaseHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.INIT.name())));
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void doneAndDeleteProjectWithNoPhase() throws Exception {
		String newProjectHref = testUtils.createRandomProject();
		testUtils.start(newProjectHref);
		testUtils.done(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));

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

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));  //because it has been done

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

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));

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

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));  //because it has been done

		mockMvc.perform(get(newPhaseHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)));


		testUtils.delete(newProjectHref);

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(newPhaseHref))
				.andExpect(status().isNotFound());
	}

	@Sql({"/cleanup.sql"})
	@Test
	public void testEvent() throws Exception {
		String projectHref = testUtils.createRandomProject();

		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(
						Event.PROJECT_CREATED,
						Event.PROJECT_OWNER_CHANGED,
						Event.PROJECT_MANAGER_CHANGED
				)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(3)));

		testUtils.start(projectHref);
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_STARTED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(4)));

		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.name,RandomStringUtils.randomAlphanumeric(10)));

		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_PROPERTIES_CHANGED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(5)));

		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.owner,RandomStringUtils.randomAlphanumeric(10)));
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_OWNER_CHANGED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(6)));

		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.manager,RandomStringUtils.randomAlphanumeric(10)));
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_MANAGER_CHANGED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(7)));

		String memberA = RandomStringUtils.randomAlphanumeric(10);
		String memberB = RandomStringUtils.randomAlphanumeric(10);
		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.members, Lists.newArrayList(memberA, memberB)));
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_MEMBER_ADDED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(8)));

		String memberC = RandomStringUtils.randomAlphanumeric(10);
		testUtils.patchUpdate(projectHref, Collections.singletonMap(Project.Fields.members, Lists.newArrayList(memberA, memberC)));
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_MEMBER_ADDED,Event.PROJECT_MEMBER_DELETED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(10)));

		testUtils.done(projectHref);
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_DONE)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(11)));

		testUtils.delete(projectHref);
		mockMvc.perform(get("/events"))
				.andExpect(jsonPath("$._embedded.events..key", hasItems(Event.PROJECT_DELETED)))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(12)));

	}
}
