
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
    public ResponseEntity<Carro> salvarCarro(@RequestBody Carro c) {
        return ResponseEntity.ok(auditoriaService.executar("CARRO", "CRIAR", () -> carroService.save(c), Carro::getId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @RequestBody Carro c) {
        c.setId(id);
        return ResponseEntity.ok(auditoriaService.executar("CARRO", "ATUALIZAR", () -> carroService.update(c), Carro::getId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id) {
        auditoriaService.executar("CARRO", "EXCLUIR", () -> {
            carroService.deleteById(id);
            return id;
        }, carroId -> carroId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = auditoriaService.executar("CARRO", "CONSULTAR_POR_ID", () -> carroService.findById(id), result -> result.map(Carro::getId).orElse(null));
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listarCarros")
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        List<Carro> carros = auditoriaService.executar("CARRO", "LISTAR", carroService::findAll, result -> null);
        return ResponseEntity.ok(carros);
    }

    @PostMapping(value = "/getCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> pesquisarCarroPorModelo(@RequestBody String modelo) {
        Optional<Carro> carro = auditoriaService.executar("CARRO", "CONSULTAR_POR_MODELO", () -> carroService.findByModelo(modelo.trim()), result -> result.map(Carro::getId).orElse(null));
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/deleteCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deletarCarroPorModelo(@RequestBody String modelo) {
        Carro carro = auditoriaService.executar("CARRO", "EXCLUIR_POR_MODELO", () -> carroService.deleteByModelo(modelo.trim()), Carro::getId);
        return ResponseEntity.ok("Carro deletado: " + carro.getModelo());
    }

    @PostMapping(value = "/updateCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> atualizarCarroPorModelo(@RequestBody String payload) {
        String[] partes = payload.split(",", 2);
        if (partes.length < 2) {
            throw new CarroException("Payload inválido. Use modelo,preco.");
        }
        String modelo = partes[0].trim();
        double preco = parsePreco(partes[1].trim());
        Carro updatedCarro = auditoriaService.executar("CARRO", "ATUALIZAR_POR_MODELO", () -> carroService.updateByModelo(modelo, preco), Carro::getId);
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