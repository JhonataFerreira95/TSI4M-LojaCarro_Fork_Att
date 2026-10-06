package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class CarroAuditoriaService {

    private final UsuarioClient usuarioClient;
    private final LogOperacaoRepository logRepository;

    public CarroAuditoriaService(UsuarioClient usuarioClient, LogOperacaoRepository logRepository) {
        this.usuarioClient = usuarioClient;
        this.logRepository = logRepository;
    }

    @Transactional
    public <T> T executar(Long usuarioId, String operacao, Supplier<T> acao, Function<T, Long> extrairCarroId) {
        UsuarioClient.UsuarioResponse usuario = usuarioClient.buscarPorId(usuarioId);
        T resultado = acao.get();
        logRepository.save(new LogOperacao(usuario.id(), usuario.nome(), operacao, extrairCarroId.apply(resultado), Instant.now()));
        return resultado;
    }
}
