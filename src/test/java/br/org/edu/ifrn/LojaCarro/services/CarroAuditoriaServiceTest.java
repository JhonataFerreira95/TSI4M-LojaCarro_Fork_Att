package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.AuditOutboxEvent;
import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import br.org.edu.ifrn.LojaCarro.repository.AuditOutboxRepository;
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
        AuditOutboxRepository outboxRepository = mock(AuditOutboxRepository.class);
        AdministradorRepository administradorRepository = mock(AdministradorRepository.class);
        Administrador administrador = mock(Administrador.class);
        when(administradorRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(administrador));
        when(administrador.getId()).thenReturn(7L);
        when(administrador.getNome()).thenReturn("Ana Silva");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("admin@example.com", "",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        CarroAuditoriaService service = new CarroAuditoriaService(outboxRepository, administradorRepository);

        String resultado = service.executar("CARRO", "CRIAR", () -> "criado", value -> 42L);

        assertEquals("criado", resultado);
        ArgumentCaptor<AuditOutboxEvent> captor = ArgumentCaptor.forClass(AuditOutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        AuditOutboxEvent event = captor.getValue();
        assertEquals("loja-carros", event.getSourceService());
        assertEquals("CARRO", event.getModule());
        assertEquals("ADMINISTRADOR", event.getActorType());
        assertEquals(7L, event.getActorId());
        assertEquals("Ana Silva", event.getActorName());
        assertEquals("CRIAR", event.getOperation());
        assertEquals(42L, event.getRecordId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void naoExecutaAcaoNemGravaLogSemAdministradorAutenticado() {
        AuditOutboxRepository outboxRepository = mock(AuditOutboxRepository.class);
        AdministradorRepository administradorRepository = mock(AdministradorRepository.class);
        CarroAuditoriaService service = new CarroAuditoriaService(outboxRepository, administradorRepository);

        assertThrows(IllegalStateException.class,
                () -> service.executar("CARRO", "EXCLUIR", () -> fail("A operação não deveria executar."), value -> null));

        verifyNoInteractions(outboxRepository);
    }
}
