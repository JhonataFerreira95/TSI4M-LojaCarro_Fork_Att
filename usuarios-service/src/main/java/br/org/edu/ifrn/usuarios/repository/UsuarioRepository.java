package br.org.edu.ifrn.usuarios.repository;

import br.org.edu.ifrn.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
