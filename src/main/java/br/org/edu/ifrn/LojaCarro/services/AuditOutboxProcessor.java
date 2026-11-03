package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.AuditOutboxEvent;
import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.AuditOutboxRepository;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditOutboxProcessor {

    private final AuditOutboxRepository outboxRepository;
    private final LogOperacaoRepository logRepository;

    public AuditOutboxProcessor(AuditOutboxRepository outboxRepository, LogOperacaoRepository logRepository) {
        this.outboxRepository = outboxRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public int processarPendentes() {
        List<AuditOutboxEvent> eventos = outboxRepository.findTop100ByProcessedAtIsNullOrderByIdAsc();
        Instant processedAt = Instant.now();
        for (AuditOutboxEvent evento : eventos) {
            logRepository.save(LogOperacao.fromOutboxEvent(evento));
            evento.markProcessed(processedAt);
        }
        return eventos.size();
    }
}
