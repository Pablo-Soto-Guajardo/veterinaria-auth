package cl.duoc.auth.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

// Token de renovación. Permite pedir un JWT nuevo sin volver a escribir la
// contraseña. En la base se guarda solo su hash SHA-256: si alguien leyera
// la tabla, no podría usar los tokens.
@Entity @Table(name = "refresh_token")
@Getter @Setter
public class RefreshToken {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @ManyToOne(optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Column(nullable = false)
  private Instant expiraEn;

  @Column(nullable = false)
  private boolean revocado;
}