package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import tech.kuiperbelt.spm.support.ApiTest;

import java.time.LocalDate;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = ProjectApiTests.MOCK_UERR)
class ProjectApiTests extends ApiTest {

	public static final String MOCK_UERR = "sjdfs.ldjfds";
	public static final String LOCATION = "location";

	@Test
	public void getProjects() throws Exception {
		mockMvc.perform(get("/projects"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.number", equalTo(0)));
	}

	@Test
	public void createProject() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = performCreateProject(newProject);

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

	@Test
	public void getMyProjects() {
		//TODO
	}

	@Test
	public void cancelInitProjectWithNoPhase() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = performCreateProject(newProject);
		mockMvc.perform(post(newProjectHref + "/actions/cancel"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.cancelled", equalTo(true)));
	}

	@Test
	public void cancelInitProjectWithPhases() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = performCreateProject(newProject);
		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		mockMvc.perform(post(newProjectHref + "/actions/cancel"))
				.andExpect(status().isNoContent());

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

	@Test
	public void deleteInitProjectWithNoPhase() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = performCreateProject(newProject);
		mockMvc.perform(delete(newProjectHref))
				.andExpect(status().isNoContent());


		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());
	}

	@Test
	public void deleteInitProjectWithPhases() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = performCreateProject(newProject);

		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		mockMvc.perform(delete(newProjectHref))
				.andExpect(status().isNoContent());


		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(fistPhaseHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(secondPhaseHref))
				.andExpect(status().isNotFound());
	}


	@Test
	public void startProjectWithNoPhase() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);
		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(false)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeDone", equalTo(true)));
	}

	@Test
	public void startProjectWithPhases() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);

		// add two phase
		String fistPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));
		String secondPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now().plusDays(20));

		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isNoContent());

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

	@Test
	public void doneAndDeleteProjectWithNoPhase() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);
		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isNoContent());

		reloadSession();

		mockMvc.perform(post(newProjectHref + "/actions/done"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));

		mockMvc.perform(delete(newProjectHref))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());
	}

	@Test
	public void doneAndDeleteProjectWithPhases() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);

		// add a phase
		String newPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));

		// start project, and first phase will be sets Running automatically
		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isNoContent());

		reloadSession();

		mockMvc.perform(post(newPhaseHref + "/actions/done"))
				.andExpect(status().isNoContent());

		reloadSession();

		mockMvc.perform(post(newProjectHref + "/actions/done"))
				.andExpect(status().isNoContent());

		reloadSession();
		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));  //because it has been done

		reloadSession();
		mockMvc.perform(delete(newProjectHref))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(newPhaseHref))
				.andExpect(status().isNotFound());
	}

	@Test
	public void cancelAndRemoveProjectWithNoPhase() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);
		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isNoContent());

		reloadSession();

		mockMvc.perform(post(newProjectHref + "/actions/cancel"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.STOP.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeDeleted", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(false)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));

		mockMvc.perform(delete(newProjectHref))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());
	}

	@Test
	public void cancelAndDeleteProjectWithPhases() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);

		// add a phase
		String newPhaseHref = testUtils.appendRandomPhase(newProjectHref, LocalDate.now(), LocalDate.now().plusDays(10));

		// start project, and first phase will be sets Running automatically
		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isNoContent());

		reloadSession();


		mockMvc.perform(post(newProjectHref + "/actions/cancel"))
				.andExpect(status().isNoContent());

		reloadSession();
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

		reloadSession();

		mockMvc.perform(delete(newProjectHref))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(newPhaseHref))
				.andExpect(status().isNotFound());
	}


	private String performCreateProject(Project newProject) throws Exception {
		return mockMvc.perform(post("/projects")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(newProject)))
					.andExpect(status().isCreated())
					.andExpect(header().exists(LOCATION))
					.andReturn()
					.getResponse()
					.getHeader(LOCATION);
	}
}
