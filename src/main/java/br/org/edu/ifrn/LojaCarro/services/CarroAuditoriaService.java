package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Service
public class CarroAuditoriaService {

    private final LogOperacaoRepository logRepository;
    private final AdministradorRepository administradorRepository;

    public CarroAuditoriaService(LogOperacaoRepository logRepository, AdministradorRepository administradorRepository) {
        this.logRepository = logRepository;
        this.administradorRepository = administradorRepository;
    }

    @Transactional
    public <T> T executar(String modulo, String operacao, Supplier<T> acao, Function<T, Long> extrairRegistroId) {
        return executar(modulo, operacao, acao, extrairRegistroId, resultado -> true);
    }

    @Transactional
    public <T> T executar(String modulo, String operacao, Supplier<T> acao,
                          Function<T, Long> extrairRegistroId, Predicate<T> registrarResultado) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Uma sessão de administrador autenticada é necessária para auditar a operação.");
        }
        Administrador administrador = administradorRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("O administrador autenticado não existe mais."));
        T resultado = acao.get();
        if (registrarResultado.test(resultado)) {
            logRepository.save(LogOperacao.paraAdministrador(administrador.getId(), administrador.getNome(),
                    modulo, operacao, extrairRegistroId.apply(resultado), Instant.now()));
        }
        return resultado;
    }
}
