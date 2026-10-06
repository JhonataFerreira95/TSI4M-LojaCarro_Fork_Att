package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogOperacaoRepository extends JpaRepository<LogOperacao, Long> {
}
