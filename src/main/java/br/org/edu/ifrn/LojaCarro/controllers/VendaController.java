package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.VendaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final VendaService service;
    private final CarroAuditoriaService auditoriaService;

    public VendaController(VendaService service, CarroAuditoriaService auditoriaService) {
        this.service = service;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> criar(@RequestBody VendaRequest request) {
        Venda venda = auditoriaService.executar("VENDA", "CRIAR", () -> service.criar(
                request.carroId(), request.usuarioId(), request.data(), request.valor(), request.status()), Venda::getId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta(venda));
    }

    @GetMapping
    public List<VendaResponse> listar() {
        return auditoriaService.executar("VENDA", "LISTAR", service::listar, vendas -> null)
                .stream().map(VendaController::resposta).toList();
    }

    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable Long id) {
        return resposta(auditoriaService.executar("VENDA", "CONSULTAR_POR_ID",
                () -> service.buscar(id), Venda::getId));
    }

    @PutMapping("/{id}")
    public VendaResponse atualizar(@PathVariable Long id, @RequestBody VendaRequest request) {
        return resposta(auditoriaService.executar("VENDA", "ATUALIZAR",
                () -> service.atualizar(id, request.carroId(), request.usuarioId(),
                        request.data(), request.valor(), request.status()), Venda::getId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        auditoriaService.executar("VENDA", "EXCLUIR", () -> {
            service.excluir(id);
            return id;
        }, registroId -> registroId);
        return ResponseEntity.noContent().build();
    }

    private static VendaResponse resposta(Venda venda) {
        return new VendaResponse(venda.getId(), venda.getCarroId(), venda.getUsuarioId(),
                venda.getData(), venda.getValor(), venda.getStatus());
    }

    public record VendaRequest(Long carroId, Long usuarioId, LocalDate data, BigDecimal valor, String status) { }
    public record VendaResponse(Long id, Long carroId, Long usuarioId, LocalDate data, BigDecimal valor, String status) { }
}
