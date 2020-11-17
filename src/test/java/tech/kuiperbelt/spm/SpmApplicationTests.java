package tech.kuiperbelt.spm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tech.kuiperbelt.spm.domain.core.Project;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class SpmApplicationTests {

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup(WebApplicationContext webApplicationContext) {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void getProjects() throws Exception {
		mockMvc.perform(get("/projects"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.number", equalTo(0)));
	}

	@WithMockUser(username = "yu.shen")
	@Test
	public void createProject() throws Exception {
		Project newProject = new Project().toBuilder()
				.name(RandomStringUtils.randomAlphanumeric(10))
				.build();

		String newProjectHref = mockMvc.perform(post("/projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newProject)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("location"))
				.andReturn()
				.getResponse()
				.getHeader("location");

		mockMvc.perform(get(newProjectHref))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", equalTo(newProject.getName())))
				.andExpect(jsonPath("$._links['self'].href", equalTo(newProjectHref)));
	}


}
