package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_drive")
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carro_id", nullable = false)
    private Long carroId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 40)
    private String status;

    protected TestDrive() {
    }

    public TestDrive(Long carroId, Long usuarioId, LocalDateTime dataHora, String status) {
        this.carroId = carroId;
        this.usuarioId = usuarioId;
        this.dataHora = dataHora;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getCarroId() { return carroId; }
    public void setCarroId(Long carroId) { this.carroId = carroId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
