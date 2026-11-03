package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.repository.LancamentoContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendaServiceTest {

    private final VendaRepository repository = mock(VendaRepository.class);
    private final CarroRepository carroRepository = mock(CarroRepository.class);
    private final LancamentoContabilRepository lancamentoRepository = mock(LancamentoContabilRepository.class);
    private final UsuarioClient usuarioClient = mock(UsuarioClient.class);
    private final VendaService service = new VendaService(repository, carroRepository, lancamentoRepository, usuarioClient);

    @Test
    void criaVendaComCarroEUsuarioExistentes() {
        when(carroRepository.existsById(2L)).thenReturn(true);
        when(usuarioClient.buscarPorId(4L)).thenReturn(new UsuarioClient.UsuarioResponse(4L, "Ana"));
        when(repository.save(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venda venda = service.criar(2L, 4L, LocalDate.now(), new BigDecimal("12500.00"), " PENDENTE ");

        assertEquals(2L, venda.getCarroId());
        assertEquals(4L, venda.getUsuarioId());
        assertEquals("PENDENTE", venda.getStatus());
        verify(repository).save(venda);
    }

    @Test
    void rejeitaCarroInexistente() {
        when(carroRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.criar(2L, 4L, LocalDate.now(), BigDecimal.TEN, "PENDENTE"));

        verifyNoInteractions(usuarioClient, repository);
    }

    @Test
    void impedeExcluirVendaComLancamentoContabil() {
        Venda venda = new Venda(2L, 4L, LocalDate.now(), BigDecimal.TEN, "PENDENTE");
        when(repository.findById(9L)).thenReturn(java.util.Optional.of(venda));
        when(lancamentoRepository.existsByVendaId(9L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.excluir(9L));

        assertEquals(409, exception.getStatusCode().value());
        verify(repository, never()).delete(venda);
    }
}
