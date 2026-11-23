package cl.duoc.auth.service;

import cl.duoc.auth.exception.TokenInvalidoException;
import cl.duoc.auth.model.RefreshToken;
import cl.duoc.auth.model.Usuario;
import cl.duoc.auth.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Administra los refresh tokens:
// 1. crear: genera un valor aleatorio y guarda solo su hash.
// 2. rotar: cada refresh token sirve UNA vez; al usarlo se revoca y se entrega otro.
// 3. si alguien reutiliza un token ya revocado (posible robo), se cierran
//    todas las sesiones de ese usuario.
@Service
public class RefreshTokenService {
  private final RefreshTokenRepository tokens;
  private final SecureRandom aleatorio = new SecureRandom();

  @Value("${app.refresh.expiration-ms}") private long expirationMs;

  public RefreshTokenService(RefreshTokenRepository tokens) {
    this.tokens = tokens;
  }

  @Transactional
  public String crear(Usuario usuario) {
    byte[] bytes = new byte[32];
    aleatorio.nextBytes(bytes);
    String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    RefreshToken token = new RefreshToken();
    token.setTokenHash(hash(valor));
    token.setUsuario(usuario);
    token.setExpiraEn(Instant.now().plusMillis(expirationMs));
    token.setRevocado(false);
    tokens.save(token);
    return valor;
  }

  // Valida el token recibido, lo revoca y devuelve al usuario dueño.
  @Transactional(noRollbackFor = TokenInvalidoException.class)
  public Usuario consumir(String valor) {
    RefreshToken token = tokens.findByTokenHash(hash(valor))
      .orElseThrow(() -> new TokenInvalidoException("Sesión no válida, vuelve a iniciar sesión"));

    if (token.isRevocado()) {
      revocarTodos(token.getUsuario());
      throw new TokenInvalidoException("La sesión ya fue utilizada, vuelve a iniciar sesión");
    }
    if (token.getExpiraEn().isBefore(Instant.now())) {
      token.setRevocado(true);
      throw new TokenInvalidoException("La sesión expiró, vuelve a iniciar sesión");
    }
    token.setRevocado(true);
    return token.getUsuario();
  }

  @Transactional
  public void revocar(String valor) {
    tokens.findByTokenHash(hash(valor)).ifPresent(token -> token.setRevocado(true));
  }

  @Transactional
  public void revocarTodos(Usuario usuario) {
    tokens.findByUsuarioAndRevocadoFalse(usuario).forEach(t -> t.setRevocado(true));
  }

  @Transactional
  public void eliminarDeUsuario(Usuario usuario) {
    tokens.deleteByUsuario(usuario);
  }

  static String hash(String valor) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 no disponible", e);
    }
  }
}