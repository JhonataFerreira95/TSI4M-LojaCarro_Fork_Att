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

    @Column(length = 40)
    private String modulo;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "administrador_id")
    private Long administradorId;

    @Column(name = "administrador_nome")
    private String administradorNome;

    @Column(name = "servico_origem", length = 80)
    private String servicoOrigem;

    @Column(name = "tipo_ator", length = 30)
    private String tipoAtor;

    @Column(name = "carro_id")
    private Long carroId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected LogOperacao() {
    }

    public LogOperacao(Long usuarioId, String usuarioNome, String operacao, Long carroId, Instant dataHora) {
        this(usuarioId, usuarioNome, "CARRO", operacao, carroId, carroId, dataHora);
    }

    public LogOperacao(Long usuarioId, String usuarioNome, String modulo, String operacao,
                       Long registroId, Instant dataHora) {
        this(usuarioId, usuarioNome, modulo, operacao,
                "CARRO".equals(modulo) ? registroId : null, registroId, dataHora);
    }

    public static LogOperacao paraAdministrador(Long administradorId, String administradorNome, String modulo,
                                                 String operacao, Long registroId, Instant dataHora) {
        return new LogOperacao(administradorId, administradorNome, modulo, operacao,
                "CARRO".equals(modulo) ? registroId : null, registroId, dataHora, true);
    }

    private LogOperacao(Long usuarioId, String usuarioNome, String modulo, String operacao,
                        Long carroId, Long registroId, Instant dataHora) {
        this(usuarioId, usuarioNome, modulo, operacao, carroId, registroId, dataHora, false);
    }

    private LogOperacao(Long usuarioId, String usuarioNome, String modulo, String operacao,
                        Long carroId, Long registroId, Instant dataHora, boolean auditadoPorAdministrador) {
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.modulo = modulo;
        this.operacao = operacao;
        this.carroId = carroId;
        this.registroId = registroId;
        this.dataHora = dataHora;
        if (auditadoPorAdministrador) {
            this.administradorId = usuarioId;
            this.administradorNome = usuarioNome;
            this.servicoOrigem = "loja-carros";
            this.tipoAtor = "ADMINISTRADOR";
        }
    }

    public static LogOperacao fromOutboxEvent(AuditOutboxEvent event) {
        LogOperacao log = new LogOperacao(
                event.getActorId() == null ? 0L : event.getActorId(),
                event.getActorName() == null ? event.getSourceService() : event.getActorName(),
                event.getModule(),
                event.getOperation(),
                "CARRO".equals(event.getModule()) ? event.getRecordId() : null,
                event.getRecordId(),
                event.getOccurredAt(),
                false);
        log.servicoOrigem = event.getSourceService();
        log.tipoAtor = event.getActorType();
        if ("ADMINISTRADOR".equals(event.getActorType())) {
            log.administradorId = event.getActorId();
            log.administradorNome = event.getActorName();
        }
        return log;
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

    public String getModulo() {
        return modulo == null ? "CARRO" : modulo;
    }

    public Long getRegistroId() {
        return registroId == null ? carroId : registroId;
    }

    public Long getAdministradorId() {
        if ("SERVICO".equals(tipoAtor)) {
            return null;
        }
        return administradorId == null ? usuarioId : administradorId;
    }

    public String getAdministradorNome() {
        if ("SERVICO".equals(tipoAtor)) {
            return null;
        }
        return administradorNome == null ? usuarioNome : administradorNome;
    }

    public String getServicoOrigem() {
        return servicoOrigem;
    }

    public String getTipoAtor() {
        return tipoAtor;
    }

    public Long getCarroId() {
        return carroId;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
