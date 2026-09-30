package br.org.edu.ifrn.LojaCarro.exception;

import br.org.edu.ifrn.LojaCarro.CarroException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCarroExceptionReturnsBadRequestAndMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCarroException(new CarroException("Invalid car"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("erro", "Invalid car"), response.getBody());
    }
}