package cl.duoc.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PerfilRequest(
  @NotBlank @Size(max = 50) String nombre,
  @NotBlank @Size(max = 100) String apellidos,
  @Size(max = 10) String password,
  @Past LocalDate fechaNacimiento,
  @NotBlank @Size(max = 300) String direccion,
  @NotBlank String region,
  @NotBlank String comuna
) {}
