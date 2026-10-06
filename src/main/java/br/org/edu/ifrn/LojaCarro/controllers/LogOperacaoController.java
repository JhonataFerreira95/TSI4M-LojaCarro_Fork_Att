package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogOperacaoController {

    private final LogOperacaoRepository logRepository;

    public LogOperacaoController(LogOperacaoRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping
    public ResponseEntity<List<LogOperacao>> listar() {
        return ResponseEntity.ok(logRepository.findAll());
    }
}
