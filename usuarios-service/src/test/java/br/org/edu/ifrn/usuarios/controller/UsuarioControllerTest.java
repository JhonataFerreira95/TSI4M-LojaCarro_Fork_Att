package br.org.edu.ifrn.usuarios.controller;

import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void criaConsultaAtualizaEExcluiUsuario() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Silva\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Silva"));

        Usuario usuario = repository.findAll().get(0);

        mockMvc.perform(get("/usuarios/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"));

        mockMvc.perform(put("/usuarios/{id}", usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Souza\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"));

        mockMvc.perform(delete("/usuarios/{id}", usuario.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/{id}", usuario.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaUsuarioSemNome() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
