package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.TestDrive;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.repository.TestDriveRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestDriveServiceTest {

    private final TestDriveRepository repository = mock(TestDriveRepository.class);
    private final CarroRepository carroRepository = mock(CarroRepository.class);
    private final UsuarioClient usuarioClient = mock(UsuarioClient.class);
    private final TestDriveService service = new TestDriveService(repository, carroRepository, usuarioClient);

    @Test
    void criaAgendamentoVinculadoACarroEUsuario() {
        when(carroRepository.existsById(3L)).thenReturn(true);
        when(usuarioClient.buscarPorId(6L)).thenReturn(new UsuarioClient.UsuarioResponse(6L, "João"));
        when(repository.save(any(TestDrive.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime dataHora = LocalDateTime.of(2026, 11, 10, 14, 30);

        TestDrive testDrive = service.criar(3L, 6L, dataHora, " AGENDADO ");

        assertEquals(dataHora, testDrive.getDataHora());
        assertEquals("AGENDADO", testDrive.getStatus());
        verify(repository).save(testDrive);
    }
}
