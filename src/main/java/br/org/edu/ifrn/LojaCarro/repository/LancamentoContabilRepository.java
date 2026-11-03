package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.LancamentoContabil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoContabilRepository extends JpaRepository<LancamentoContabil, Long> {
    boolean existsByVendaId(Long vendaId);
}
