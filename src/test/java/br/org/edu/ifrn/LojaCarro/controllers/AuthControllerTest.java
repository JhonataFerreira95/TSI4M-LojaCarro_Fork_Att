package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@TestPropertySource(properties = {
        "admin.bootstrap.name=Bootstrap Admin",
        "admin.bootstrap.email=bootstrap@example.com",
        "admin.bootstrap.password=bootstrap-password"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Test
    void loginCriaSessaoProtegidaELogoutEncerraAcesso() throws Exception {
        assertNotNull(administradorRepository.findByEmailIgnoreCase("bootstrap@example.com").orElseThrow());
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String csrfToken = com.jayway.jsonpath.JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.token");
        MockHttpSession csrfSession = (MockHttpSession) csrfResult.getRequest().getSession(false);
        assertNotNull(csrfSession);
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .session(csrfSession)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"bootstrap@example.com","senha":"bootstrap-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bootstrap Admin"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bootstrap@example.com"));

        MvcResult logoutCsrf = mockMvc.perform(get("/auth/csrf").session(session))
                .andExpect(status().isOk())
                .andReturn();
        String logoutToken = com.jayway.jsonpath.JsonPath.read(logoutCsrf.getResponse().getContentAsString(), "$.token");
        mockMvc.perform(post("/auth/logout").header("X-XSRF-TOKEN", logoutToken).session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejeitaSenhaInvalidaEApisExigemSessao() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"bootstrap@example.com","senha":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/administradores").with(anonymous()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/administradores")
                        .with(anonymous())
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Atacante","email":"atacante@example.com","senha":"senha-segura"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
