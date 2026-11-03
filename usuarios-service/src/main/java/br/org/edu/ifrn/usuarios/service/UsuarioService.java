package br.org.edu.ifrn.usuarios.service;

import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.model.AuditOutboxEvent;
import br.org.edu.ifrn.usuarios.repository.AuditOutboxRepository;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final AuditOutboxRepository outboxRepository;

    public UsuarioService(UsuarioRepository repository, AuditOutboxRepository outboxRepository) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public Usuario criar(Usuario usuario) {
        validarNome(usuario.getNome());
        usuario.setNome(usuario.getNome().trim());
        Usuario criado = repository.save(usuario);
        registrar("CRIAR", criado.getId());
        return criado;
    }

    @Transactional
    public Usuario buscarPorId(Long id) {
        validarId(id);
        Usuario usuario = carregar(id);
        registrar("CONSULTAR_POR_ID", id);
        return usuario;
    }

    @Transactional
    public List<Usuario> listar() {
        List<Usuario> usuarios = repository.findAll();
        registrar("LISTAR", null);
        return usuarios;
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dados) {
        validarId(id);
        validarNome(dados.getNome());
        Usuario usuario = carregar(id);
        usuario.setNome(dados.getNome().trim());
        Usuario atualizado = repository.save(usuario);
        registrar("ATUALIZAR", id);
        return atualizado;
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = carregar(id);
        repository.delete(usuario);
        registrar("EXCLUIR", id);
    }

    private Usuario carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private void registrar(String operacao, Long registroId) {
        outboxRepository.save(new AuditOutboxEvent("usuarios-service", "USUARIO", operacao, registroId,
                "SERVICO", null, "usuarios-service", Instant.now()));
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
