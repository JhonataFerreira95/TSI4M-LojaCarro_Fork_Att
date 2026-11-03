package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carro_id", nullable = false)
    private Long carroId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 40)
    private String status;

    protected Venda() {
    }

    public Venda(Long carroId, Long usuarioId, LocalDate data, BigDecimal valor, String status) {
        this.carroId = carroId;
        this.usuarioId = usuarioId;
        this.data = data;
        this.valor = valor;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getCarroId() { return carroId; }
    public void setCarroId(Long carroId) { this.carroId = carroId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
