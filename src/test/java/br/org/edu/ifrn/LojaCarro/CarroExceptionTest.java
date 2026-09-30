package br.org.edu.ifrn.LojaCarro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarroExceptionTest {

    @Test
    void constructorPreservesMessage() {
        CarroException exception = new CarroException("Invalid car");

        assertEquals("Invalid car", exception.getMessage());
    }
}