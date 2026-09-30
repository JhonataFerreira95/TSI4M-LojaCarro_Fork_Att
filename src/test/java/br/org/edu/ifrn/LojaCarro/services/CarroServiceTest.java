package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    @Test
    void saveValidatesAndReturnsSavedCar() {
        Carro carro = new Carro("Fusc", 1970, 12000.0);
        when(carroRepository.save(carro)).thenReturn(carro);

        Carro result = carroService.save(carro);

        assertSame(carro, result);
        verify(carroRepository).save(carro);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "Modelo"})
    void saveRejectsInvalidModel(String modelo) {
        Carro carro = new Carro(modelo, 2020, 1000.0);

        assertThrows(CarroException.class, () -> carroService.save(carro));
        verifyNoInteractions(carroRepository);
    }

    @Test
    void saveRejectsNegativePrice() {
        Carro carro = new Carro("Civic", 2020, -1.0);

        assertThrows(CarroException.class, () -> carroService.save(carro));
        verifyNoInteractions(carroRepository);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void deleteByIdRejectsNonPositiveIds(long id) {
        assertThrows(CarroException.class, () -> carroService.deleteById(id));
        verifyNoInteractions(carroRepository);
    }

    @Test
    void deleteByIdDelegatesForPositiveId() {
        carroService.deleteById(4L);

        verify(carroRepository).deleteById(4L);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void findByIdRejectsNonPositiveIds(long id) {
        assertThrows(CarroException.class, () -> carroService.findById(id));
        verifyNoInteractions(carroRepository);
    }

    @Test
    void findByIdReturnsRepositoryResult() {
        Carro carro = new Carro("Civic", 2020);
        when(carroRepository.findById(4L)).thenReturn(Optional.of(carro));

        assertEquals(Optional.of(carro), carroService.findById(4L));
    }

    @Test
    void findAllReturnsRepositoryResult() {
        List<Carro> carros = List.of(new Carro("Civic", 2020));
        when(carroRepository.findAll()).thenReturn(carros);

        assertSame(carros, carroService.findAll());
    }

    @Test
    void findByModeloValidatesAndReturnsRepositoryResult() {
        Carro carro = new Carro("Civi", 2020);
        when(carroRepository.findFirstByModelo("Civi")).thenReturn(Optional.of(carro));

        assertEquals(Optional.of(carro), carroService.findByModelo("Civi"));
        verify(carroRepository).findFirstByModelo("Civi");
    }

    @Test
    void saveFromLegacyUsesCurrentYearAndSavesCar() {
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carro result = carroService.saveFromLegacy("Civi", 1000.0);

        assertEquals(LocalDate.now().getYear(), result.getAno());
        assertEquals("Civi", result.getModelo());
        assertEquals(1000.0, result.getPreco());
        verify(carroRepository).save(any(Carro.class));
    }

    @Test
    void updateByModeloUpdatesAndSavesCar() {
        Carro carro = new Carro("Civi", 2020, 1000.0);
        when(carroRepository.findFirstByModelo("Civi")).thenReturn(Optional.of(carro));
        when(carroRepository.save(carro)).thenReturn(carro);

        Carro result = carroService.updateByModelo("Civi", 2000.0);

        assertSame(carro, result);
        assertEquals(2000.0, result.getPreco());
        verify(carroRepository).save(carro);
    }

    @Test
    void updateByModeloRejectsNegativePrice() {
        when(carroRepository.findFirstByModelo("Civi"))
            .thenReturn(Optional.of(new Carro("Civi", 2020, 1000.0)));

        assertThrows(CarroException.class, () -> carroService.updateByModelo("Civi", -1.0));
        verify(carroRepository, never()).save(any(Carro.class));
    }

    @Test
    void deleteByModeloDeletesAndReturnsFoundCar() {
        Carro carro = new Carro("Civi", 2020);
        when(carroRepository.findFirstByModelo("Civi")).thenReturn(Optional.of(carro));

        assertSame(carro, carroService.deleteByModelo("Civi"));

        verify(carroRepository).delete(carro);
    }

    @Test
    void updateRejectsNullId() {
        assertThrows(CarroException.class, () -> carroService.update(new Carro("Civi", 2020)));
        verifyNoInteractions(carroRepository);
    }

    @Test
    void updateRejectsUnknownId() {
        Carro carro = new Carro("Civi", 2020);
        carro.setId(7L);
        when(carroRepository.existsById(7L)).thenReturn(false);

        assertThrows(CarroException.class, () -> carroService.update(carro));
        verify(carroRepository, never()).save(any(Carro.class));
    }

    @Test
    void updateSavesExistingCarAfterValidation() {
        Carro carro = new Carro("Civi", 2020, 1000.0);
        carro.setId(7L);
        when(carroRepository.existsById(7L)).thenReturn(true);
        when(carroRepository.save(carro)).thenReturn(carro);

        assertSame(carro, carroService.update(carro));

        verify(carroRepository).save(carro);
    }
}