package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.AdministradorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/administradores")
public class AdministradorController {

    private final AdministradorService service;
    private final CarroAuditoriaService auditoriaService;

    public AdministradorController(AdministradorService service, CarroAuditoriaService auditoriaService) {
        this.service = service;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    public ResponseEntity<AdministradorResponse> criar(@RequestBody AdministradorRequest request) {
        Administrador administrador = auditoriaService.executar("ADMINISTRADOR", "CRIAR",
                () -> service.criar(request.nome(), request.email(), request.senha()), Administrador::getId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta(administrador));
    }

    @GetMapping
    public List<AdministradorResponse> listar() {
        return auditoriaService.executar("ADMINISTRADOR", "LISTAR", service::listar,
                administradores -> null).stream().map(AdministradorController::resposta).toList();
    }

    @GetMapping("/{id}")
    public AdministradorResponse buscar(@PathVariable Long id) {
        return resposta(auditoriaService.executar("ADMINISTRADOR", "CONSULTAR_POR_ID",
                () -> service.buscar(id), Administrador::getId));
    }

    @PutMapping("/{id}")
    public AdministradorResponse atualizar(@PathVariable Long id, @RequestBody AdministradorRequest request) {
        return resposta(auditoriaService.executar("ADMINISTRADOR", "ATUALIZAR",
                () -> service.atualizar(id, request.nome(), request.email(), request.senha()), Administrador::getId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
        if (service.buscar(id).getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é permitido excluir o administrador da sessão atual.");
        }
        auditoriaService.executar("ADMINISTRADOR", "EXCLUIR", () -> {
            service.excluir(id);
            return id;
        }, registroId -> registroId);
        return ResponseEntity.noContent().build();
    }

    private static AdministradorResponse resposta(Administrador administrador) {
        return new AdministradorResponse(administrador.getId(), administrador.getNome(), administrador.getEmail());
    }

    public record AdministradorRequest(String nome, String email, String senha) {
    }

    public record AdministradorResponse(Long id, String nome, String email) {
    }
}
