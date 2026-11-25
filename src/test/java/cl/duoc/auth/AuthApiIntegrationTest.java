package cl.duoc.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

// Prueba de integración: levanta el backend completo (con H2) en un puerto
// aleatorio y lo llama por HTTP, igual que lo haría React o Postman.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthApiIntegrationTest {
  @Value("${local.server.port}") int puerto;
  private final HttpClient cliente = HttpClient.newHttpClient();

  private HttpResponse<String> enviar(String metodo, String ruta, String json, String token)
      throws Exception {
    HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
      .header("Content-Type", "application/json")
      .method(metodo, json == null ? HttpRequest.BodyPublishers.noBody()
        : HttpRequest.BodyPublishers.ofString(json));
    if (token != null) builder.header("Authorization", "Bearer " + token);
    return cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static String campo(String json, String nombre) {
    Matcher m = Pattern.compile("\"" + nombre + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
    assertTrue(m.find(), "No se encontró " + nombre + " en " + json);
    return m.group(1);
  }

  private String login(String correo, String password) throws Exception {
    var r = enviar("POST", "/api/v1/auth/login",
      "{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}", null);
    assertEquals(200, r.statusCode());
    return r.body();
  }

  @Test
  void loginValidoEntrega200YJwt() throws Exception {
    String cuerpo = login("admin@duoc.cl", "admin123");
    assertEquals("ADMIN", campo(cuerpo, "rol"));
    assertEquals(3, campo(cuerpo, "token").split("\\.").length, "Un JWT tiene 3 partes");
  }

  @Test
  void loginIncorrectoEntrega401() throws Exception {
    var r = enviar("POST", "/api/v1/auth/login",
      "{\"correo\":\"admin@duoc.cl\",\"password\":\"mala\"}", null);
    assertEquals(401, r.statusCode());
  }

  @Test
  void usuariosSinTokenEntrega401() throws Exception {
    assertEquals(401, enviar("GET", "/api/v1/usuarios", null, null).statusCode());
  }

  @Test
  void usuariosConRolClienteEntrega403() throws Exception {
    String token = campo(login("cliente@gmail.com", "cliente1"), "token");
    assertEquals(403, enviar("GET", "/api/v1/usuarios", null, token).statusCode());
  }

  @Test
  void usuariosConRolAdminEntrega200() throws Exception {
    String token = campo(login("admin@duoc.cl", "admin123"), "token");
    var r = enviar("GET", "/api/v1/usuarios", null, token);
    assertEquals(200, r.statusCode());
    assertTrue(r.body().contains("vendedor@duoc.cl"));
    assertFalse(r.body().contains("password"), "La API nunca expone contraseñas");
  }

  @Test
  void perfilPropioFuncionaParaCualquierRol() throws Exception {
    String token = campo(login("vendedor@duoc.cl", "vende123"), "token");
    var r = enviar("GET", "/api/v1/usuarios/me", null, token);
    assertEquals(200, r.statusCode());
    assertEquals("VENDEDOR", campo(r.body(), "rol"));
  }

  @Test
  void refreshRotaElTokenYNoPermiteReutilizarlo() throws Exception {
    String refresh = campo(login("cliente@gmail.com", "cliente1"), "refreshToken");
    String json = "{\"refreshToken\":\"" + refresh + "\"}";

    var primera = enviar("POST", "/api/v1/auth/refresh", json, null);
    assertEquals(200, primera.statusCode());
    assertNotEquals(refresh, campo(primera.body(), "refreshToken"));

    var reutilizado = enviar("POST", "/api/v1/auth/refresh", json, null);
    assertEquals(401, reutilizado.statusCode());
  }

  @Test
  void tokenAlteradoEntrega401() throws Exception {
    String token = campo(login("admin@duoc.cl", "admin123"), "token");
    String alterado = token.substring(0, token.length() - 2) + "xx";
    assertEquals(401, enviar("GET", "/api/v1/usuarios", null, alterado).statusCode());
  }
}