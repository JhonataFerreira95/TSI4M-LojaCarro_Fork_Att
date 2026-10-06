package br.org.edu.ifrn.usuarios.service;

import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario criar(Usuario usuario) {
        validarNome(usuario.getNome());
        usuario.setNome(usuario.getNome().trim());
        return repository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        validarId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario atualizar(Long id, Usuario dados) {
        validarId(id);
        validarNome(dados.getNome());
        Usuario usuario = buscarPorId(id);
        usuario.setNome(dados.getNome().trim());
        return repository.save(usuario);
    }

    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);
        repository.delete(usuario);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do usuário é obrigatório.");
        }
        if (nome.trim().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome deve ter no máximo 120 caracteres.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do usuário deve ser positivo.");
        }
    }
}
