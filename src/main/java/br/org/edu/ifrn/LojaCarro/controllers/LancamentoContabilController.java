package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.LancamentoContabil;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.LancamentoContabilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/lancamentos-contabeis")
public class LancamentoContabilController {

    private final LancamentoContabilService service;
    private final CarroAuditoriaService auditoriaService;

    public LancamentoContabilController(LancamentoContabilService service, CarroAuditoriaService auditoriaService) {
        this.service = service;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    public ResponseEntity<LancamentoResponse> criar(@RequestBody LancamentoRequest request) {
        LancamentoContabil lancamento = auditoriaService.executar("CONTABILIDADE", "CRIAR", () -> service.criar(
                request.vendaId(), request.tipoLancamento(), request.valor(),
                request.vencimento(), request.pagamento(), request.status()), LancamentoContabil::getId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta(lancamento));
    }

    @GetMapping
    public List<LancamentoResponse> listar() {
        return auditoriaService.executar("CONTABILIDADE", "LISTAR", service::listar, lancamentos -> null)
                .stream().map(LancamentoContabilController::resposta).toList();
    }

    @GetMapping("/{id}")
    public LancamentoResponse buscar(@PathVariable Long id) {
        return resposta(auditoriaService.executar("CONTABILIDADE", "CONSULTAR_POR_ID",
                () -> service.buscar(id), LancamentoContabil::getId));
    }

    @PutMapping("/{id}")
    public LancamentoResponse atualizar(@PathVariable Long id, @RequestBody LancamentoRequest request) {
        return resposta(auditoriaService.executar("CONTABILIDADE", "ATUALIZAR",
                () -> service.atualizar(id, request.vendaId(), request.tipoLancamento(), request.valor(),
                        request.vencimento(), request.pagamento(), request.status()), LancamentoContabil::getId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        auditoriaService.executar("CONTABILIDADE", "EXCLUIR", () -> {
            service.excluir(id);
            return id;
        }, registroId -> registroId);
        return ResponseEntity.noContent().build();
    }

    private static LancamentoResponse resposta(LancamentoContabil lancamento) {
        return new LancamentoResponse(lancamento.getId(), lancamento.getVendaId(), lancamento.getTipoLancamento(),
                lancamento.getValor(), lancamento.getVencimento(), lancamento.getPagamento(), lancamento.getStatus());
    }

    public record LancamentoRequest(Long vendaId, String tipoLancamento, BigDecimal valor, LocalDate vencimento,
                                    LocalDate pagamento, String status) { }
    public record LancamentoResponse(Long id, Long vendaId, String tipoLancamento, BigDecimal valor,
                                     LocalDate vencimento, LocalDate pagamento, String status) { }
}
