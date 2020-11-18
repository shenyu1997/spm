package tech.kuiperbelt.spm.domain.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import tech.kuiperbelt.spm.ApiTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = ProjectApiTests.MOCK_UERR)
class ProjectApiTests extends ApiTest {

	public static final String MOCK_UERR = "sjdfs.ldjfds";

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
				.andExpect(jsonPath("$.canBeRemoved", equalTo(true)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)))
				.andExpect(jsonPath("$._links['self'].href", equalTo(newProjectHref)));
	}


	@Test
	public void startProjectWithNoPhase() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();
		String newProjectHref = performCreateProject(newProject);
		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isOk());

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeRemoved", equalTo(false)))
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
		appendRandomPhase(newProjectHref, Optional.of(LocalDate.now()), LocalDate.now().plusDays(10));
		appendRandomPhase(newProjectHref, Optional.empty(), LocalDate.now().plusDays(20));

		mockMvc.perform(post(newProjectHref + "/actions/start"))
				.andExpect(status().isOk());

		mockMvc.perform(get(newProjectHref))
				.andExpect(jsonPath("$.status", equalTo(RunningStatus.RUNNING.name())))
				.andExpect(jsonPath("$.canBeStarted", equalTo(false)))
				.andExpect(jsonPath("$.canBeRemoved", equalTo(false)))
				.andExpect(jsonPath("$.canBeCancelled", equalTo(true)))
				.andExpect(jsonPath("$.canBeDone", equalTo(false)));  //because there are Non STOP phase
	}

	private void appendRandomPhase(String newProjectHref, Optional<LocalDate> plannedStartDate, LocalDate plannedEndDate) throws Exception {
		Phase.PhaseBuilder phaseBuilder = new Phase().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(6))
				.plannedEndDate(plannedEndDate);
		plannedStartDate.ifPresent(phaseBuilder::plannedStartDate);
		Phase phase = phaseBuilder.build();

		mockMvc.perform(post(newProjectHref + "/phases/actions/append")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(phase)))
				.andExpect(status().isCreated());
		reloadSession();
	}

	private String performCreateProject(Project newProject) throws Exception {
		return mockMvc.perform(post("/projects")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(newProject)))
					.andExpect(status().isCreated())
					.andExpect(header().exists("location"))
					.andReturn()
					.getResponse()
					.getHeader("location");
	}
}
