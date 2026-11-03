package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.AuditOutboxProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogOperacaoController {

    private final LogOperacaoRepository logRepository;
    private final CarroAuditoriaService auditoriaService;
    private final AuditOutboxProcessor outboxProcessor;

    public LogOperacaoController(LogOperacaoRepository logRepository, CarroAuditoriaService auditoriaService,
                                 AuditOutboxProcessor outboxProcessor) {
        this.logRepository = logRepository;
        this.auditoriaService = auditoriaService;
        this.outboxProcessor = outboxProcessor;
    }

    @GetMapping
    public ResponseEntity<List<LogOperacao>> listar() {
        return ResponseEntity.ok(auditoriaService.executar("AUDITORIA", "LISTAR_LOGS", () -> {
            outboxProcessor.processarPendentes();
            return logRepository.findAll();
        }, logs -> null));
    }
}
