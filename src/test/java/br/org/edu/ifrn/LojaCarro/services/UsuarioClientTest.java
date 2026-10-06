package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsuarioClientTest {

    @Test
    void consultaNomePelaApiDeUsuarios() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://usuarios.test/usuarios/12"))
                .andExpect(request -> assertEquals(HttpMethod.GET, request.getMethod()))
                .andRespond(withSuccess("{\"id\":12,\"nome\":\"Ana Silva\"}", MediaType.APPLICATION_JSON));
        UsuarioClient client = new UsuarioClient(builder, "http://usuarios.test");

        UsuarioClient.UsuarioResponse usuario = client.buscarPorId(12L);

        assertEquals(12L, usuario.id());
        assertEquals("Ana Silva", usuario.nome());
        server.verify();
    }

    @Test
    void retornaErroDeUsuarioInexistenteQuandoApiResponde404() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://usuarios.test/usuarios/99"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));
        UsuarioClient client = new UsuarioClient(builder, "http://usuarios.test");

        assertThrows(UsuarioNaoEncontradoException.class, () -> client.buscarPorId(99L));
        server.verify();
    }
}
