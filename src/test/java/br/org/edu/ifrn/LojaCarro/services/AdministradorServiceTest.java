package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdministradorServiceTest {

    private final AdministradorRepository repository = mock(AdministradorRepository.class);
    private final AdministradorService service = new AdministradorService(repository);

    @Test
    void criaAdministradorComEmailNormalizadoESenhaHash() {
        when(repository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Administrador result = service.criar(" Ana ", " ANA@EXEMPLO.COM ", "senha-segura");

        assertEquals("Ana", result.getNome());
        assertEquals("ana@exemplo.com", result.getEmail());
        assertNotEquals("senha-segura", result.getSenhaHash());
        assertTrue(new BCryptPasswordEncoder().matches("senha-segura", result.getSenhaHash()));
    }

    @Test
    void rejeitaSenhaFracaSemPersistir() {
        assertThrows(ResponseStatusException.class, () -> service.criar("Ana", "ana@exemplo.com", "curta"));
        verifyNoInteractions(repository);
    }

    @Test
    void atualizacaoSemSenhaMantemHashExistente() {
        Administrador administrador = new Administrador("Ana", "ana@exemplo.com", "hash-existente");
        when(repository.findById(3L)).thenReturn(java.util.Optional.of(administrador));
        when(repository.save(administrador)).thenReturn(administrador);

        Administrador result = service.atualizar(3L, "Ana Silva", "ana@exemplo.com", "");

        assertEquals("Ana Silva", result.getNome());
        assertEquals("hash-existente", result.getSenhaHash());
    }
}
