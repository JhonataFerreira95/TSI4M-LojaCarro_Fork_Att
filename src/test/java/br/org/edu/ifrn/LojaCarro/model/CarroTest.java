package br.org.edu.ifrn.LojaCarro.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CarroTest {

    @Test
    void noArgumentConstructorInitializesDefaultValues() {
        Carro carro = new Carro();

        assertNull(carro.getId());
        assertNull(carro.getModelo());
        assertEquals(0, carro.getAno());
        assertEquals(0.0, carro.getPreco());
    }

    @Test
    void twoArgumentConstructorDefaultsPriceToZero() {
        Carro carro = new Carro("Civi", 2020);

        assertEquals("Civi", carro.getModelo());
        assertEquals(2020, carro.getAno());
        assertEquals(0.0, carro.getPreco());
    }

    @Test
    void threeArgumentConstructorStoresAllValues() {
        Carro carro = new Carro("Civi", 2020, 1250.5);

        assertEquals("Civi", carro.getModelo());
        assertEquals(2020, carro.getAno());
        assertEquals(1250.5, carro.getPreco());
    }

    @Test
    void accessorsUpdateAllFields() {
        Carro carro = new Carro();

        carro.setId(3L);
        carro.setModelo("Civi");
        carro.setAno(2021);
        carro.setPreco(900.0);

        assertEquals(3L, carro.getId());
        assertEquals("Civi", carro.getModelo());
        assertEquals(2021, carro.getAno());
        assertEquals(900.0, carro.getPreco());
    }
}