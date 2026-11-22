package cl.duoc.auth.dto;

import cl.duoc.auth.model.Usuario;
import java.time.LocalDate;

public record UsuarioResponse(
  String run,
  String nombre,
  String apellidos,
  String correo,
  String rol,
  LocalDate fechaNacimiento,
  String direccion,
  String region,
  String comuna
) {
  public static UsuarioResponse desde(Usuario u) {
    return new UsuarioResponse(u.getRun(), u.getNombre(), u.getApellidos(),
      u.getCorreo(), u.getRol().name(), u.getFechaNacimiento(),
      u.getDireccion(), u.getRegion(), u.getComuna());
  }
}
