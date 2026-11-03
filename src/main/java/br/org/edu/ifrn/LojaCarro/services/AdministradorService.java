package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AdministradorService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final AdministradorRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdministradorService(AdministradorRepository repository) {
        this.repository = repository;
    }

    public List<Administrador> listar() {
        return repository.findAll();
    }

    public Administrador buscar(Long id) {
        validarId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));
    }

    public Administrador criar(String nome, String email, String senha) {
        validarDados(nome, email, senha, true);
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um administrador com este e-mail.");
        }
        return repository.save(new Administrador(nome.trim(), emailNormalizado, passwordEncoder.encode(senha)));
    }

    public Administrador atualizar(Long id, String nome, String email, String senha) {
        validarId(id);
        Administrador administrador = buscar(id);
        validarDados(nome, email, senha, false);
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCaseAndIdNot(emailNormalizado, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um administrador com este e-mail.");
        }
        administrador.setNome(nome.trim());
        administrador.setEmail(emailNormalizado);
        if (senha != null && !senha.isBlank()) {
            administrador.setSenhaHash(passwordEncoder.encode(senha));
        }
        return repository.save(administrador);
    }

    public void excluir(Long id) {
        if (repository.count() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é permitido excluir o último administrador.");
        }
        repository.delete(buscar(id));
    }

    private void validarDados(String nome, String email, String senha, boolean senhaObrigatoria) {
        if (nome == null || nome.isBlank() || nome.trim().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório e deve ter até 120 caracteres.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches() || email.trim().length() > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido com até 180 caracteres.");
        }
        if (senhaObrigatoria && (senha == null || senha.length() < 8)
                || !senhaObrigatoria && senha != null && !senha.isBlank() && senha.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 8 caracteres.");
        }
        if (senha != null && senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter no máximo 72 bytes UTF-8.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do administrador deve ser positivo.");
        }
    }
}
