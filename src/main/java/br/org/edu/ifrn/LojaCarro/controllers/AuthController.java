package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Administrador;
import br.org.edu.ifrn.LojaCarro.repository.AdministradorRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AdministradorRepository administradorRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, AdministradorRepository administradorRepository,
                          CsrfTokenRepository csrfTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.administradorRepository = administradorRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
        return new CsrfResponse(csrfToken.getToken());
    }

    @PostMapping("/login")
    public AdministradorResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, servletRequest, servletResponse);
            return administradorAutenticado(authentication.getName());
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
    }

    @GetMapping("/me")
    public AdministradorResponse me(Authentication authentication) {
        return administradorAutenticado(authentication.getName());
    }

    private AdministradorResponse administradorAutenticado(String email) {
        Administrador administrador = administradorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não encontrado."));
        return new AdministradorResponse(administrador.getId(), administrador.getNome(), administrador.getEmail());
    }

    public record LoginRequest(String email, String senha) {
    }

    public record CsrfResponse(String token) {
    }

    public record AdministradorResponse(Long id, String nome, String email) {
    }
}
