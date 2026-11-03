package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.LancamentoContabil;
import br.org.edu.ifrn.LojaCarro.repository.LancamentoContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LancamentoContabilService {

    private final LancamentoContabilRepository repository;
    private final VendaRepository vendaRepository;

    public LancamentoContabilService(LancamentoContabilRepository repository, VendaRepository vendaRepository) {
        this.repository = repository;
        this.vendaRepository = vendaRepository;
    }

    public List<LancamentoContabil> listar() { return repository.findAll(); }

    public LancamentoContabil buscar(Long id) {
        validarId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento contábil não encontrado."));
    }

    public LancamentoContabil criar(Long vendaId, String tipoLancamento, BigDecimal valor, LocalDate vencimento,
                                     LocalDate pagamento, String status) {
        validar(vendaId, tipoLancamento, valor, vencimento, pagamento, status);
        return repository.save(new LancamentoContabil(vendaId, tipoLancamento.trim(), valor,
                vencimento, pagamento, status.trim()));
    }

    public LancamentoContabil atualizar(Long id, Long vendaId, String tipoLancamento, BigDecimal valor,
                                         LocalDate vencimento, LocalDate pagamento, String status) {
        LancamentoContabil lancamento = buscar(id);
        validar(vendaId, tipoLancamento, valor, vencimento, pagamento, status);
        lancamento.setVendaId(vendaId);
        lancamento.setTipoLancamento(tipoLancamento.trim());
        lancamento.setValor(valor);
        lancamento.setVencimento(vencimento);
        lancamento.setPagamento(pagamento);
        lancamento.setStatus(status.trim());
        return repository.save(lancamento);
    }

    public void excluir(Long id) { repository.delete(buscar(id)); }

    private void validar(Long vendaId, String tipoLancamento, BigDecimal valor, LocalDate vencimento,
                         LocalDate pagamento, String status) {
        if (vendaId == null || vendaId <= 0 || !vendaRepository.existsById(vendaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma venda existente.");
        }
        if (tipoLancamento == null || tipoLancamento.isBlank() || tipoLancamento.trim().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo de lançamento é obrigatório e deve ter até 40 caracteres.");
        }
        if (valor == null || valor.signum() <= 0 || valor.scale() > 2 || valor.precision() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor deve ser positivo e ter até duas casas decimais.");
        }
        if (vencimento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O vencimento é obrigatório.");
        }
        if (status == null || status.isBlank() || status.trim().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O status é obrigatório e deve ter até 40 caracteres.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do lançamento deve ser positivo.");
        }
    }
}
