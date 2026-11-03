package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@WithMockUser(username = "test-admin@example.com", roles = "ADMIN")
@org.springframework.test.context.TestPropertySource(properties = {
        "admin.bootstrap.name=Test Admin",
        "admin.bootstrap.email=test-admin@example.com",
        "admin.bootstrap.password=test-admin-password"
})
class AdministradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository repository;

    @Test
    void criaListaConsultaAtualizaEExcluiSemExporSenha() throws Exception {
        mockMvc.perform(post("/administradores")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Silva","email":"ANA@EXEMPLO.COM","senha":"senha-segura"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        Long id = repository.findByEmailIgnoreCase("ana@exemplo.com").orElseThrow().getId();
        mockMvc.perform(get("/administradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'ana@exemplo.com')]").exists())
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist());

        mockMvc.perform(put("/administradores/{id}", id).with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Souza","email":"ana@exemplo.com","senha":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        mockMvc.perform(delete("/administradores/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
    }
}
