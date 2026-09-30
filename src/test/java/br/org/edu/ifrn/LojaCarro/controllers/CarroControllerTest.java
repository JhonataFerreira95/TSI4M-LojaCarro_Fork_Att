package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
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

    @InjectMocks
    private CarroController carroController;

    @Test
    void salvarCarroReturnsSavedCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.save(carro)).thenReturn(carro);

        ResponseEntity<Carro> response = carroController.salvarCarro(carro);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
    }

    @Test
    void atualizarCarroSetsPathIdAndReturnsUpdatedCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.update(carro)).thenReturn(carro);

        ResponseEntity<Carro> response = carroController.atualizarCarro(8L, carro);

        assertEquals(8L, carro.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
        verify(carroService).update(carro);
    }

    @Test
    void deletarCarroReturnsNoContent() {
        ResponseEntity<Void> response = carroController.deletarCarro(8L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(carroService).deleteById(8L);
    }

    @Test
    void pesquisarCarroPorIdReturnsFoundCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.findById(8L)).thenReturn(Optional.of(carro));

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorId(8L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
    }

    @Test
    void pesquisarCarroPorIdReturnsNotFoundWhenAbsent() {
        when(carroService.findById(8L)).thenReturn(Optional.empty());

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorId(8L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void pesquisarTodosCarrosReturnsList() {
        List<Carro> carros = List.of(new Carro("Civi", 2020));
        when(carroService.findAll()).thenReturn(carros);

        ResponseEntity<List<Carro>> response = carroController.pesquisarTodosCarros();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carros, response.getBody());
    }

    @Test
    void pesquisarCarroPorModeloTrimsPayloadAndReturnsCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroService.findByModelo("Civi")).thenReturn(Optional.of(carro));

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorModelo(" Civi ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
        verify(carroService).findByModelo("Civi");
    }

    @Test
    void pesquisarCarroPorModeloReturnsNotFoundWhenAbsent() {
        when(carroService.findByModelo("Civi")).thenReturn(Optional.empty());

        ResponseEntity<Carro> response = carroController.pesquisarCarroPorModelo("Civi");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deletarCarroPorModeloTrimsPayloadAndReturnsMessage() {
        when(carroService.deleteByModelo("Civi")).thenReturn(new Carro("Civi", 2020));

        ResponseEntity<String> response = carroController.deletarCarroPorModelo(" Civi ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Carro deletado: Civi", response.getBody());
        verify(carroService).deleteByModelo("Civi");
    }

    @Test
    void atualizarCarroPorModeloParsesPayloadAndReturnsUpdatedCar() {
        Carro carro = new Carro("Civi", 2020, 2500.0);
        when(carroService.updateByModelo("Civi", 2500.0)).thenReturn(carro);

        ResponseEntity<Carro> response = carroController.atualizarCarroPorModelo(" Civi , 2500.0 ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(carro, response.getBody());
        verify(carroService).updateByModelo("Civi", 2500.0);
    }

    @Test
    void atualizarCarroPorModeloRejectsMissingPrice() {
        assertThrows(CarroException.class, () -> carroController.atualizarCarroPorModelo("Civi"));
        verifyNoInteractions(carroService);
    }

    @Test
    void atualizarCarroPorModeloRejectsInvalidPrice() {
        assertThrows(CarroException.class, () -> carroController.atualizarCarroPorModelo("Civi,abc"));
        verifyNoInteractions(carroService);
    }

    @Test
    void bomDiaTrimsName() {
        ResponseEntity<String> response = carroController.bomDia(" Bass ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bom dia, Bass", response.getBody());
    }
}