package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.AuditOutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface AuditOutboxRepository extends JpaRepository<AuditOutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<AuditOutboxEvent> findTop100ByProcessedAtIsNullOrderByIdAsc();
}
