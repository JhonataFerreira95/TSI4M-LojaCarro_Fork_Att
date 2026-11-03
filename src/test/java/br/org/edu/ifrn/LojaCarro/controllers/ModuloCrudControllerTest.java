package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.repository.LancamentoContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.TestDriveRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import br.org.edu.ifrn.LojaCarro.services.UsuarioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@WithMockUser(username = "test-admin@example.com", roles = "ADMIN")
@org.springframework.test.context.TestPropertySource(properties = {
        "admin.bootstrap.name=Test Admin",
        "admin.bootstrap.email=test-admin@example.com",
        "admin.bootstrap.password=test-admin-password"
})
class ModuloCrudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private TestDriveRepository testDriveRepository;

    @Autowired
    private LancamentoContabilRepository lancamentoRepository;

    @Autowired
    private LogOperacaoRepository logRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @MockitoBean
    private UsuarioClient usuarioClient;

    private Carro carro;

    @BeforeEach
    void prepararBanco() {
        lancamentoRepository.deleteAll();
        logRepository.deleteAll();
        testDriveRepository.deleteAll();
        vendaRepository.deleteAll();
        carroRepository.deleteAll();
        carro = carroRepository.save(new Carro("Civi", 2024, 25000.00));
        logRepository.deleteAll();
        when(usuarioClient.buscarPorId(7L)).thenReturn(new UsuarioClient.UsuarioResponse(7L, "Ana Silva"));
    }

    @Test
    void criaAtualizaConsultaEExcluiVendaTestDriveELancamento() throws Exception {
        mockMvc.perform(post("/vendas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carroId":%d,"usuarioId":7,"data":"2026-11-03","valor":25000.00,"status":"PENDENTE"}
                                """.formatted(carro.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carroId").value(carro.getId()))
                .andExpect(jsonPath("$.usuarioId").value(7));

        Long vendaId = vendaRepository.findAll().get(0).getId();
        mockMvc.perform(get("/vendas/{id}", vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mockMvc.perform(put("/vendas/{id}", vendaId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carroId":%d,"usuarioId":7,"data":"2026-11-03","valor":24000.00,"status":"CONCLUIDA"}
                                """.formatted(carro.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(24000.00));

        mockMvc.perform(post("/lancamentos-contabeis")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendaId":%d,"tipoLancamento":"RECEITA","valor":24000.00,"vencimento":"2026-12-01","pagamento":null,"status":"PENDENTE"}
                                """.formatted(vendaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendaId").value(vendaId));

        Long lancamentoId = lancamentoRepository.findAll().get(0).getId();
        mockMvc.perform(delete("/vendas/{id}", vendaId).with(csrf()))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/lancamentos-contabeis/{id}", lancamentoId).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/vendas/{id}", vendaId).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/test-drives")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carroId":%d,"usuarioId":7,"dataHora":"2026-11-10T14:30:00","status":"AGENDADO"}
                                """.formatted(carro.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"));

        Long testDriveId = testDriveRepository.findAll().get(0).getId();
        mockMvc.perform(put("/test-drives/{id}", testDriveId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carroId":%d,"usuarioId":7,"dataHora":"2026-11-10T15:00:00","status":"CONCLUIDO"}
                                """.formatted(carro.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
        mockMvc.perform(get("/test-drives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(7));
        mockMvc.perform(delete("/test-drives/{id}", testDriveId).with(csrf()))
                .andExpect(status().isNoContent());

        var logs = logRepository.findAll();
        assertTrue(logs.stream().anyMatch(log -> log.getModulo().equals("VENDA")
                && log.getOperacao().equals("CRIAR") && log.getRegistroId().equals(vendaId)
                && log.getAdministradorId().equals(administradorRepository.findByEmailIgnoreCase("test-admin@example.com").orElseThrow().getId())));
        assertTrue(logs.stream().anyMatch(log -> log.getModulo().equals("CONTABILIDADE")
                && log.getOperacao().equals("CRIAR") && log.getRegistroId().equals(lancamentoId)));
        assertTrue(logs.stream().anyMatch(log -> log.getModulo().equals("TESTE_DRIVE")
                && log.getOperacao().equals("CRIAR") && log.getRegistroId().equals(testDriveId)));
        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modulo == 'VENDA')]").exists())
                .andExpect(jsonPath("$[?(@.modulo == 'TESTE_DRIVE')]").exists())
                .andExpect(jsonPath("$[?(@.modulo == 'CONTABILIDADE')]").exists());
    }

    @Test
    void exigeOperadorParaConsultarModulosELogs() throws Exception {
        mockMvc.perform(get("/vendas").with(anonymous())).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/logs").with(anonymous())).andExpect(status().isUnauthorized());
    }

    @Test
    void logIdentificaOperadorAutenticadoIndependenteDoClienteDaVenda() throws Exception {
        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk());
        var registro = logRepository.findAll().get(0);
        assertTrue(registro.getAdministradorNome().equals("Test Admin"));
        assertTrue(registro.getModulo().equals("VENDA"));
    }
}
