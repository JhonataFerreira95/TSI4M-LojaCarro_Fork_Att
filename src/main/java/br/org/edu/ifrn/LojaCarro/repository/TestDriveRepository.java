package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {
    boolean existsByCarroId(Long carroId);
    boolean existsByUsuarioId(Long usuarioId);
}
