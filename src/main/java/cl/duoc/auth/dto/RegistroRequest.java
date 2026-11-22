package cl.duoc.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegistroRequest(
  @NotBlank @Pattern(regexp = Reglas.RUN, message = "RUN sin puntos ni guion (ej: 190110222)") String run,
  @NotBlank @Size(max = 50) String nombre,
  @NotBlank @Size(max = 100) String apellidos,
  @NotBlank @Size(max = 100) @Pattern(regexp = Reglas.CORREO, message = Reglas.MENSAJE_CORREO) String correo,
  @NotBlank @Size(min = 4, max = 10) String password,
  @Past LocalDate fechaNacimiento,
  @NotBlank @Size(max = 300) String direccion,
  @NotBlank String region,
  @NotBlank String comuna
) {}
