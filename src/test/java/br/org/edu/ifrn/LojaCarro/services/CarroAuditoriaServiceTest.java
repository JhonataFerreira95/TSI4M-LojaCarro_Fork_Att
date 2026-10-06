package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarroAuditoriaServiceTest {

    @Test
    void registraOperacaoComUsuarioRetornadoPelaApi() {
        UsuarioClient usuarioClient = mock(UsuarioClient.class);
        LogOperacaoRepository logRepository = mock(LogOperacaoRepository.class);
        when(usuarioClient.buscarPorId(7L)).thenReturn(new UsuarioClient.UsuarioResponse(7L, "Ana Silva"));
        CarroAuditoriaService service = new CarroAuditoriaService(usuarioClient, logRepository);

        String resultado = service.executar(7L, "CRIAR", () -> "criado", value -> 42L);

        assertEquals("criado", resultado);
        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logRepository).save(captor.capture());
        LogOperacao log = captor.getValue();
        assertEquals(7L, log.getUsuarioId());
        assertEquals("Ana Silva", log.getUsuarioNome());
        assertEquals("CRIAR", log.getOperacao());
        assertEquals(42L, log.getCarroId());
        assertNotNull(log.getDataHora());
    }

    @Test
    void naoExecutaOperacaoNemGravaLogQuandoUsuarioNaoPodeSerConsultado() {
        UsuarioClient usuarioClient = mock(UsuarioClient.class);
        LogOperacaoRepository logRepository = mock(LogOperacaoRepository.class);
        when(usuarioClient.buscarPorId(7L)).thenThrow(new IllegalStateException("serviço indisponível"));
        CarroAuditoriaService service = new CarroAuditoriaService(usuarioClient, logRepository);

        assertThrows(IllegalStateException.class,
                () -> service.executar(7L, "EXCLUIR", () -> fail("A operação não deveria executar."), value -> null));

        verifyNoInteractions(logRepository);
    }
}
