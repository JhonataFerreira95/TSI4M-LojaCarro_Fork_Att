package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.repository.TestDriveRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/usuarios")
public class UsuarioProxyController {

    private final RestClient usuariosServiceClient;
    private final VendaRepository vendaRepository;
    private final TestDriveRepository testDriveRepository;
    private final CarroAuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    public UsuarioProxyController(RestClient.Builder builder, @Value("${usuarios.api.url}") String usuariosApiUrl,
                                  VendaRepository vendaRepository, TestDriveRepository testDriveRepository,
                                  CarroAuditoriaService auditoriaService, ObjectMapper objectMapper) {
        this.usuariosServiceClient = builder.baseUrl(usuariosApiUrl).build();
        this.vendaRepository = vendaRepository;
        this.testDriveRepository = testDriveRepository;
        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<byte[]> listar() {
        return auditoriaService.executar("USUARIO", "LISTAR",
                () -> encaminhar(usuariosServiceClient.get().uri("/usuarios")), response -> null,
                response -> response.getStatusCode().is2xxSuccessful());
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> buscar(@PathVariable Long id) {
        return auditoriaService.executar("USUARIO", "CONSULTAR_POR_ID",
                () -> encaminhar(usuariosServiceClient.get().uri("/usuarios/{id}", id)), response -> id,
                response -> response.getStatusCode().is2xxSuccessful());
    }

    @PostMapping
    public ResponseEntity<byte[]> criar(@RequestBody byte[] usuario) {
        return auditoriaService.executar("USUARIO", "CRIAR",
                () -> encaminhar(usuariosServiceClient.post()
                        .uri("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(usuario)), this::usuarioIdDaResposta,
                response -> response.getStatusCode().is2xxSuccessful());
    }

    @PutMapping("/{id}")
    public ResponseEntity<byte[]> atualizar(@PathVariable Long id, @RequestBody byte[] usuario) {
        return auditoriaService.executar("USUARIO", "ATUALIZAR",
                () -> encaminhar(usuariosServiceClient.put()
                        .uri("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(usuario)), response -> id,
                response -> response.getStatusCode().is2xxSuccessful());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<byte[]> excluir(@PathVariable Long id) {
        return auditoriaService.executar("USUARIO", "EXCLUIR",
                () -> {
                    if (vendaRepository.existsByUsuarioId(id) || testDriveRepository.existsByUsuarioId(id)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "O usuário não pode ser excluído enquanto estiver vinculado a vendas ou test drives.");
                    }
                    return encaminhar(usuariosServiceClient.delete().uri("/usuarios/{id}", id));
                },
                response -> id, response -> response.getStatusCode().is2xxSuccessful());
    }

    private Long usuarioIdDaResposta(ResponseEntity<byte[]> response) {
        if (response.getBody() == null || response.getBody().length == 0) {
            throw new IllegalStateException("O serviço de usuários não retornou o usuário criado para registrar a auditoria.");
        }
        try {
            JsonNode body = objectMapper.readTree(response.getBody());
            JsonNode id = body == null ? null : body.get("id");
            if (id == null || !id.canConvertToLong()) {
                throw new IllegalStateException("O serviço de usuários retornou uma resposta sem ID para registrar a auditoria.");
            }
            return id.longValue();
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível ler o ID do usuário criado para registrar a auditoria.", ex);
        }
    }

    private ResponseEntity<byte[]> encaminhar(RestClient.RequestHeadersSpec<?> request) {
        return request.exchange((clientRequest, clientResponse) -> {
            HttpHeaders headers = new HttpHeaders();
            MediaType contentType = clientResponse.getHeaders().getContentType();
            if (contentType != null) {
                headers.setContentType(contentType);
            }
            return new ResponseEntity<>(
                    clientResponse.getBody().readAllBytes(),
                    headers,
                    clientResponse.getStatusCode());
        });
    }
}
