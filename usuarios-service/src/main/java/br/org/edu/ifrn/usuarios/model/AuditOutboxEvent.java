package br.org.edu.ifrn.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_outbox")
public class AuditOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_service", nullable = false, length = 80)
    private String sourceService;

    @Column(name = "module_name", nullable = false, length = 40)
    private String module;

    @Column(name = "operation_name", nullable = false, length = 80)
    private String operation;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "actor_type", nullable = false, length = 30)
    private String actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name", length = 120)
    private String actorName;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected AuditOutboxEvent() {
    }

    public AuditOutboxEvent(String sourceService, String module, String operation, Long recordId,
                            String actorType, Long actorId, String actorName, Instant occurredAt) {
        this.sourceService = sourceService;
        this.module = module;
        this.operation = operation;
        this.recordId = recordId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.actorName = actorName;
        this.occurredAt = occurredAt;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getActorName() {
        return actorName;
    }
}
