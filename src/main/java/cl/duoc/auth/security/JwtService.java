package cl.duoc.auth.security;

import cl.duoc.auth.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Auth es el único servicio que FIRMA tokens (Guía 17).
// También los valida, porque protege sus propios endpoints de usuarios.
@Service
public class JwtService {
  public static final String ISSUER = "veterinaria-auth";

  @Value("${app.jwt.secret}") private String secret;
  @Value("${app.jwt.expiration-ms}") private long expirationMs;

  private SecretKey clave() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String generar(Usuario usuario) {
    Date ahora = new Date();
    return Jwts.builder()
      .issuer(ISSUER)
      .subject(usuario.getCorreo())
      .claim("rol", usuario.getRol().name())
      .claim("nombre", usuario.getNombre())
      .issuedAt(ahora)
      .expiration(new Date(ahora.getTime() + expirationMs))
      .signWith(clave())
      .compact();
  }

  // Comprueba firma, emisor y expiración. Si algo falla lanza JwtException.
  public Claims validar(String token) {
    return Jwts.parser()
      .verifyWith(clave())
      .requireIssuer(ISSUER)
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  public long getExpiracionSegundos() {
    return expirationMs / 1000;
  }
}