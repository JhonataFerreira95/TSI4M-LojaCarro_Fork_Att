package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "log_operacao")
public class LogOperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_nome", nullable = false)
    private String usuarioNome;

    @Column(nullable = false)
    private String operacao;

    @Column(name = "carro_id")
    private Long carroId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected LogOperacao() {
    }

    public LogOperacao(Long usuarioId, String usuarioNome, String operacao, Long carroId, Instant dataHora) {
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.operacao = operacao;
        this.carroId = carroId;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public String getOperacao() {
        return operacao;
    }

    public Long getCarroId() {
        return carroId;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
