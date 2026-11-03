package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarroAuditoriaServiceTest {

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraOperacaoComAdministradorAutenticado() {
        LogOperacaoRepository logRepository = mock(LogOperacaoRepository.class);
        AdministradorRepository administradorRepository = mock(AdministradorRepository.class);
        Administrador administrador = mock(Administrador.class);
        when(administradorRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(administrador));
        when(administrador.getId()).thenReturn(7L);
        when(administrador.getNome()).thenReturn("Ana Silva");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("admin@example.com", "",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        CarroAuditoriaService service = new CarroAuditoriaService(logRepository, administradorRepository);

        String resultado = service.executar("CARRO", "CRIAR", () -> "criado", value -> 42L);

        assertEquals("criado", resultado);
        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logRepository).save(captor.capture());
        LogOperacao log = captor.getValue();
        assertEquals(7L, log.getUsuarioId());
        assertEquals("Ana Silva", log.getUsuarioNome());
        assertEquals(7L, log.getAdministradorId());
        assertEquals("Ana Silva", log.getAdministradorNome());
        assertEquals("CRIAR", log.getOperacao());
        assertEquals(42L, log.getCarroId());
        assertNotNull(log.getDataHora());
    }

    @Test
    void naoExecutaAcaoNemGravaLogSemAdministradorAutenticado() {
        LogOperacaoRepository logRepository = mock(LogOperacaoRepository.class);
        AdministradorRepository administradorRepository = mock(AdministradorRepository.class);
        CarroAuditoriaService service = new CarroAuditoriaService(logRepository, administradorRepository);

        assertThrows(IllegalStateException.class,
                () -> service.executar("CARRO", "EXCLUIR", () -> fail("A operação não deveria executar."), value -> null));

        verifyNoInteractions(logRepository);
    }
}
