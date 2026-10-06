
package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroAuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/carro")
public class CarroController {

    private final CarroService carroService;
    private final CarroAuditoriaService auditoriaService;

    public CarroController(CarroService carroService, CarroAuditoriaService auditoriaService) {
        this.carroService = carroService;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping("salvar")
    public ResponseEntity<Carro> salvarCarro(@RequestHeader("X-User-Id") Long usuarioId, @RequestBody Carro c) {
        return ResponseEntity.ok(auditoriaService.executar(usuarioId, "CRIAR", () -> carroService.save(c), carro -> carro.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@RequestHeader("X-User-Id") Long usuarioId, @PathVariable Long id, @RequestBody Carro c) {
        c.setId(id);
        return ResponseEntity.ok(auditoriaService.executar(usuarioId, "ATUALIZAR", () -> carroService.update(c), carro -> carro.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@RequestHeader("X-User-Id") Long usuarioId, @PathVariable Long id) {
        auditoriaService.executar(usuarioId, "EXCLUIR", () -> {
            carroService.deleteById(id);
            return id;
        }, carroId -> carroId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@RequestHeader("X-User-Id") Long usuarioId, @PathVariable Long id) {
        Optional<Carro> carro = auditoriaService.executar(usuarioId, "CONSULTAR_POR_ID", () -> carroService.findById(id), result -> result.map(c -> c.getId()).orElse(null));
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listarCarros")
    public ResponseEntity<List<Carro>> pesquisarTodosCarros(@RequestHeader("X-User-Id") Long usuarioId) {
        List<Carro> carros = auditoriaService.executar(usuarioId, "LISTAR", carroService::findAll, result -> null);
        return ResponseEntity.ok(carros);
    }

    @PostMapping(value = "/getCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> pesquisarCarroPorModelo(@RequestHeader("X-User-Id") Long usuarioId, @RequestBody String modelo) {
        Optional<Carro> carro = auditoriaService.executar(usuarioId, "CONSULTAR_POR_MODELO", () -> carroService.findByModelo(modelo.trim()), result -> result.map(c -> c.getId()).orElse(null));
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/deleteCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deletarCarroPorModelo(@RequestHeader("X-User-Id") Long usuarioId, @RequestBody String modelo) {
        Carro carro = auditoriaService.executar(usuarioId, "EXCLUIR_POR_MODELO", () -> carroService.deleteByModelo(modelo.trim()), item -> item.getId());
        return ResponseEntity.ok("Carro deletado: " + carro.getModelo());
    }

    @PostMapping(value = "/updateCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> atualizarCarroPorModelo(@RequestHeader("X-User-Id") Long usuarioId, @RequestBody String payload) {
        String[] partes = payload.split(",", 2);
        if (partes.length < 2) {
            throw new CarroException("Payload inválido. Use modelo,preco.");
        }
        String modelo = partes[0].trim();
        double preco = parsePreco(partes[1].trim());
        Carro updatedCarro = auditoriaService.executar(usuarioId, "ATUALIZAR_POR_MODELO", () -> carroService.updateByModelo(modelo, preco), carro -> carro.getId());
        return ResponseEntity.ok(updatedCarro);
    }

    @PostMapping(value = "/teste", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> bomDia(@RequestBody String nome) {
        return ResponseEntity.ok("Bom dia, " + nome.trim());
    }

    private double parsePreco(String preco) {
        try {
            return Double.parseDouble(preco);
        } catch (NumberFormatException ex) {
            throw new CarroException("Preço inválido: " + preco);
        }
    }
}