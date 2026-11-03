package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.TestDrive;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.TestDriveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/test-drives")
public class TestDriveController {

    private final TestDriveService service;
    private final CarroAuditoriaService auditoriaService;

    public TestDriveController(TestDriveService service, CarroAuditoriaService auditoriaService) {
        this.service = service;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    public ResponseEntity<TestDriveResponse> criar(@RequestBody TestDriveRequest request) {
        TestDrive testDrive = auditoriaService.executar("TESTE_DRIVE", "CRIAR", () -> service.criar(
                request.carroId(), request.usuarioId(), request.dataHora(), request.status()), TestDrive::getId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta(testDrive));
    }

    @GetMapping
    public List<TestDriveResponse> listar() {
        return auditoriaService.executar("TESTE_DRIVE", "LISTAR", service::listar, testDrives -> null)
                .stream().map(TestDriveController::resposta).toList();
    }

    @GetMapping("/{id}")
    public TestDriveResponse buscar(@PathVariable Long id) {
        return resposta(auditoriaService.executar("TESTE_DRIVE", "CONSULTAR_POR_ID",
                () -> service.buscar(id), TestDrive::getId));
    }

    @PutMapping("/{id}")
    public TestDriveResponse atualizar(@PathVariable Long id, @RequestBody TestDriveRequest request) {
        return resposta(auditoriaService.executar("TESTE_DRIVE", "ATUALIZAR",
                () -> service.atualizar(id, request.carroId(), request.usuarioId(), request.dataHora(), request.status()),
                TestDrive::getId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        auditoriaService.executar("TESTE_DRIVE", "EXCLUIR", () -> {
            service.excluir(id);
            return id;
        }, registroId -> registroId);
        return ResponseEntity.noContent().build();
    }

    private static TestDriveResponse resposta(TestDrive testDrive) {
        return new TestDriveResponse(testDrive.getId(), testDrive.getCarroId(), testDrive.getUsuarioId(),
                testDrive.getDataHora(), testDrive.getStatus());
    }

    public record TestDriveRequest(Long carroId, Long usuarioId, LocalDateTime dataHora, String status) { }
    public record TestDriveResponse(Long id, Long carroId, Long usuarioId, LocalDateTime dataHora, String status) { }
}
