package cl.duoc.auth.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

// Tabla USUARIO del modelo de datos (Anexo 1).
// Vive en la base veterinaria_auth: el backend Tienda solo conoce
// al usuario por el correo que viaja dentro del JWT.
@Entity @Table(name = "usuario")
@Getter @Setter
public class Usuario {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 9)
  private String run;

  @Column(nullable = false, length = 50)
  private String nombre;

  @Column(nullable = false, length = 100)
  private String apellidos;

  @Column(nullable = false, unique = true, length = 100)
  private String correo;

  // Se guarda solo el hash BCrypt, nunca la contraseña original.
  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Rol rol;

  private LocalDate fechaNacimiento;

  @Column(nullable = false, length = 300)
  private String direccion;

  @Column(nullable = false, length = 10)
  private String region;

  @Column(nullable = false, length = 60)
  private String comuna;
}