package br.org.edu.ifrn.LojaCarro.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditOutboxScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AuditOutboxScheduler.class);

    private final AuditOutboxProcessor outboxProcessor;

    public AuditOutboxScheduler(AuditOutboxProcessor outboxProcessor) {
        this.outboxProcessor = outboxProcessor;
    }

    @Scheduled(fixedDelayString = "${audit.outbox.poll-interval-ms:1000}")
    public void processarAgendado() {
        try {
            outboxProcessor.processarPendentes();
        } catch (RuntimeException exception) {
            logger.error("Falha ao processar eventos pendentes da auditoria; eles serão tentados novamente.", exception);
        }
    }
}
