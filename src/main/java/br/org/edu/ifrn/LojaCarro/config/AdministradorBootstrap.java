package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import br.org.edu.ifrn.LojaCarro.services.AdministradorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdministradorBootstrap implements ApplicationRunner {

    private final AdministradorRepository repository;
    private final AdministradorService service;
    private final String nome;
    private final String email;
    private final String senha;

    public AdministradorBootstrap(AdministradorRepository repository, AdministradorService service,
                                  @Value("${admin.bootstrap.name:}") String nome,
                                  @Value("${admin.bootstrap.email:}") String email,
                                  @Value("${admin.bootstrap.password:}") String senha) {
        this.repository = repository;
        this.service = service;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean configurado = !nome.isBlank() || !email.isBlank() || !senha.isBlank();
        if (!configurado) {
            return;
        }
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            throw new IllegalStateException("Configure ADMIN_BOOTSTRAP_NAME, ADMIN_BOOTSTRAP_EMAIL e ADMIN_BOOTSTRAP_PASSWORD juntos.");
        }
        if (repository.count() == 0) {
            service.criar(nome, email, senha);
        }
    }
}
