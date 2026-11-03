package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.TestDrive;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.repository.TestDriveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestDriveService {

    private final TestDriveRepository repository;
    private final CarroRepository carroRepository;
    private final UsuarioClient usuarioClient;

    public TestDriveService(TestDriveRepository repository, CarroRepository carroRepository, UsuarioClient usuarioClient) {
        this.repository = repository;
        this.carroRepository = carroRepository;
        this.usuarioClient = usuarioClient;
    }

    public List<TestDrive> listar() { return repository.findAll(); }

    public TestDrive buscar(Long id) {
        validarId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento de test drive não encontrado."));
    }

    public TestDrive criar(Long carroId, Long usuarioId, LocalDateTime dataHora, String status) {
        validar(carroId, usuarioId, dataHora, status);
        return repository.save(new TestDrive(carroId, usuarioId, dataHora, status.trim()));
    }

    public TestDrive atualizar(Long id, Long carroId, Long usuarioId, LocalDateTime dataHora, String status) {
        TestDrive testDrive = buscar(id);
        validar(carroId, usuarioId, dataHora, status);
        testDrive.setCarroId(carroId);
        testDrive.setUsuarioId(usuarioId);
        testDrive.setDataHora(dataHora);
        testDrive.setStatus(status.trim());
        return repository.save(testDrive);
    }

    public void excluir(Long id) { repository.delete(buscar(id)); }

    private void validar(Long carroId, Long usuarioId, LocalDateTime dataHora, String status) {
        if (carroId == null || carroId <= 0 || !carroRepository.existsById(carroId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um carro existente.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um usuário válido.");
        }
        usuarioClient.buscarPorId(usuarioId);
        if (dataHora == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data e hora do test drive são obrigatórias.");
        }
        if (status == null || status.isBlank() || status.trim().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O status é obrigatório e deve ter até 40 caracteres.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do test drive deve ser positivo.");
        }
    }
}
