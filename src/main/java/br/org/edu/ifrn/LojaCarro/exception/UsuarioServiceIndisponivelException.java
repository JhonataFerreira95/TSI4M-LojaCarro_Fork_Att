package br.org.edu.ifrn.LojaCarro.exception;

public class UsuarioServiceIndisponivelException extends RuntimeException {
    public UsuarioServiceIndisponivelException(String message) {
        super(message);
    }

    public UsuarioServiceIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
