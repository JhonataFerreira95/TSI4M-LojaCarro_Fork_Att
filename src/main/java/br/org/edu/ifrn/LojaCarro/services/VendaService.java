package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.repository.LancamentoContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository repository;
    private final CarroRepository carroRepository;
    private final LancamentoContabilRepository lancamentoRepository;
    private final UsuarioClient usuarioClient;

    public VendaService(VendaRepository repository, CarroRepository carroRepository,
                        LancamentoContabilRepository lancamentoRepository, UsuarioClient usuarioClient) {
        this.repository = repository;
        this.carroRepository = carroRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.usuarioClient = usuarioClient;
    }

    public List<Venda> listar() { return repository.findAll(); }

    public Venda buscar(Long id) {
        validarId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));
    }

    public Venda criar(Long carroId, Long usuarioId, LocalDate data, BigDecimal valor, String status) {
        validar(carroId, usuarioId, data, valor, status);
        return repository.save(new Venda(carroId, usuarioId, data, valor, status.trim()));
    }

    public Venda atualizar(Long id, Long carroId, Long usuarioId, LocalDate data, BigDecimal valor, String status) {
        Venda venda = buscar(id);
        validar(carroId, usuarioId, data, valor, status);
        venda.setCarroId(carroId);
        venda.setUsuarioId(usuarioId);
        venda.setData(data);
        venda.setValor(valor);
        venda.setStatus(status.trim());
        return repository.save(venda);
    }

    public void excluir(Long id) {
        Venda venda = buscar(id);
        if (lancamentoRepository.existsByVendaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A venda não pode ser excluída enquanto houver lançamentos contábeis vinculados.");
        }
        repository.delete(venda);
    }

    private void validar(Long carroId, Long usuarioId, LocalDate data, BigDecimal valor, String status) {
        if (carroId == null || carroId <= 0 || !carroRepository.existsById(carroId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um carro existente.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um usuário válido.");
        }
        usuarioClient.buscarPorId(usuarioId);
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data da venda é obrigatória.");
        }
        if (valor == null || valor.signum() <= 0 || valor.scale() > 2 || valor.precision() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor deve ser positivo e ter até duas casas decimais.");
        }
        if (status == null || status.isBlank() || status.trim().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O status é obrigatório e deve ter até 40 caracteres.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da venda deve ser positivo.");
        }
    }
}
