package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento_contabil")
public class LancamentoContabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venda_id", nullable = false)
    private Long vendaId;

    @Column(name = "tipo_lancamento", nullable = false, length = 40)
    private String tipoLancamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(name = "data_pagamento")
    private LocalDate pagamento;

    @Column(nullable = false, length = 40)
    private String status;

    protected LancamentoContabil() {
    }

    public LancamentoContabil(Long vendaId, String tipoLancamento, BigDecimal valor,
                              LocalDate vencimento, LocalDate pagamento, String status) {
        this.vendaId = vendaId;
        this.tipoLancamento = tipoLancamento;
        this.valor = valor;
        this.vencimento = vencimento;
        this.pagamento = pagamento;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getVendaId() { return vendaId; }
    public void setVendaId(Long vendaId) { this.vendaId = vendaId; }
    public String getTipoLancamento() { return tipoLancamento; }
    public void setTipoLancamento(String tipoLancamento) { this.tipoLancamento = tipoLancamento; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getVencimento() { return vencimento; }
    public void setVencimento(LocalDate vencimento) { this.vencimento = vencimento; }
    public LocalDate getPagamento() { return pagamento; }
    public void setPagamento(LocalDate pagamento) { this.pagamento = pagamento; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
