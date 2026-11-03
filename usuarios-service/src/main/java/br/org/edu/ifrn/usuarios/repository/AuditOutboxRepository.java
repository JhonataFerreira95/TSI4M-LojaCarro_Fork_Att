package br.org.edu.ifrn.usuarios.repository;

import br.org.edu.ifrn.usuarios.model.AuditOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditOutboxRepository extends JpaRepository<AuditOutboxEvent, Long> {
}
