package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.LancamentoContabil;
import br.org.edu.ifrn.LojaCarro.repository.LancamentoContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LancamentoContabilServiceTest {

    private final LancamentoContabilRepository repository = mock(LancamentoContabilRepository.class);
    private final VendaRepository vendaRepository = mock(VendaRepository.class);
    private final LancamentoContabilService service = new LancamentoContabilService(repository, vendaRepository);

    @Test
    void criaLancamentoRelacionadoAVendaExistente() {
        when(vendaRepository.existsById(5L)).thenReturn(true);
        when(repository.save(any(LancamentoContabil.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LancamentoContabil result = service.criar(5L, " RECEITA ", new BigDecimal("500.00"),
                LocalDate.of(2026, 12, 1), null, " PENDENTE ");

        assertEquals(5L, result.getVendaId());
        assertEquals("RECEITA", result.getTipoLancamento());
        assertEquals("PENDENTE", result.getStatus());
    }

    @Test
    void rejeitaVendaInexistente() {
        when(vendaRepository.existsById(5L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.criar(5L, "RECEITA",
                BigDecimal.TEN, LocalDate.now(), null, "PENDENTE"));
        verifyNoInteractions(repository);
    }
}
