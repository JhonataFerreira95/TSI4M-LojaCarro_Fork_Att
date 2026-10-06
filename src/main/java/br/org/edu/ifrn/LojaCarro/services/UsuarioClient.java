package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.exception.UsuarioNaoEncontradoException;
import br.org.edu.ifrn.LojaCarro.exception.UsuarioServiceIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
public class UsuarioClient {

    private final RestClient restClient;

    public UsuarioClient(RestClient.Builder builder, @Value("${usuarios.api.url:http://localhost:8081}") String baseUrl) {
        this.restClient = builder.baseUrl(Objects.requireNonNull(baseUrl, "usuarios.api.url não pode ser nula")).build();
    }

    public UsuarioResponse buscarPorId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do usuário deve ser positivo.");
        }

        try {
            UsuarioResponse usuario = restClient.get()
                    .uri("/usuarios/{id}", usuarioId)
                    .retrieve()
                    .body(UsuarioResponse.class);
            if (usuario == null || usuario.nome() == null || usuario.nome().isBlank()) {
                throw new UsuarioServiceIndisponivelException("O serviço de usuários retornou uma resposta inválida.");
            }
            return usuario;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new UsuarioNaoEncontradoException("Usuário com ID " + usuarioId + " não encontrado.");
            }
            throw new UsuarioServiceIndisponivelException("Falha no serviço de usuários: HTTP " + ex.getStatusCode().value(), ex);
        } catch (ResourceAccessException ex) {
            throw new UsuarioServiceIndisponivelException("Não foi possível conectar ao serviço de usuários.", ex);
        }
    }

    public record UsuarioResponse(Long id, String nome) {
    }
}
