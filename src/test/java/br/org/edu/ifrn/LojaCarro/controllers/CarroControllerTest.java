package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarroControllerTest {

    @Mock
    private CarroService carroService;

    @Mock
    private CarroAuditoriaService auditoriaService;

    @InjectMocks
    private CarroController carroController;

    @BeforeEach
    void executarAcaoAuditoriaNosTestes() {
        lenient().doAnswer(invocation -> {
            java.util.function.Supplier<?> acao = invocation.getArgument(2);
            return acao.get();
        }).when(auditoriaService).executar(anyLong(), anyString(), any(), any());
    }

    @Test
    void salvarCarroReturnsSavedCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.save(carro)).thenReturn(carro);

        ResponseEntity<Carro> response = carroController.salvarCarro(1L, carro);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
    }

    @Test
    void atualizarCarroSetsPathIdAndReturnsUpdatedCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.update(carro)).thenReturn(carro);

        ResponseEntity<Carro> response = carroController.atualizarCarro(1L, 8L, carro);

        assertEquals(8L, carro.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
        verify(carroService).update(carro);
    }

    @Test
    void deletarCarroReturnsNoContent() {
        ResponseEntity<Void> response = carroController.deletarCarro(1L, 8L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(carroService).deleteById(8L);
    }

    @Test
    void pesquisarCarroPorIdReturnsFoundCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.findById(8L)).thenReturn(Optional.of(carro));

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorId(1L, 8L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
    }

    @Test
    void pesquisarCarroPorIdReturnsNotFoundWhenAbsent() {
        when(carroService.findById(8L)).thenReturn(Optional.empty());

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorId(1L, 8L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void pesquisarTodosCarrosReturnsList() {
        List<Carro> carros = List.of(new Carro("Civi", 2020));
        when(carroService.findAll()).thenReturn(carros);

        ResponseEntity<List<Carro>> response = carroController.pesquisarTodosCarros(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carros, response.getBody());
    }

    @Test
    void pesquisarCarroPorModeloTrimsPayloadAndReturnsCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.findByModelo("Civi")).thenReturn(Optional.of(carro));

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorModelo(1L, " Civi ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
        verify(carroService).findByModelo("Civi");
    }

    @Test
    void pesquisarCarroPorModeloReturnsNotFoundWhenAbsent() {
        when(carroService.findByModelo("Civi")).thenReturn(Optional.empty());

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorModelo(1L, "Civi");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deletarCarroPorModeloTrimsPayloadAndReturnsMessage() {
        when(carroService.deleteByModelo("Civi")).thenReturn(new Carro("Civi", 2020));

        ResponseEntity<String> response = carroController.deletarCarroPorModelo(1L, " Civi ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Carro deletado: Civi", response.getBody());
        verify(carroService).deleteByModelo("Civi");
    }

    @Test
    void atualizarCarroPorModeloParsesPayloadAndReturnsUpdatedCar() {
        Carro carro = new Carro("Civi", 2020, 2500.0);
        when(carroService.updateByModelo("Civi", 2500.0)).thenReturn(carro);

        ResponseEntity<Carro> response = carroController.atualizarCarroPorModelo(1L, " Civi , 2500.0 ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
        verify(carroService).updateByModelo("Civi", 2500.0);
    }

    @Test
    void atualizarCarroPorModeloRejectsMissingPrice() {
        assertThrows(CarroException.class, () -> carroController.atualizarCarroPorModelo(1L, "Civi"));
        verifyNoInteractions(carroService);
    }

    @Test
    void atualizarCarroPorModeloRejectsInvalidPrice() {
        assertThrows(CarroException.class, () -> carroController.atualizarCarroPorModelo(1L, "Civi,abc"));
        verifyNoInteractions(carroService);
    }

    @Test
    void bomDiaTrimsName() {
        ResponseEntity<String> response = carroController.bomDia(" Bass ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bom dia, Bass", response.getBody());
    }
}