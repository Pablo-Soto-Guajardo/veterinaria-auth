package cl.duoc.auth.dto;

public record LoginResponse(
  String token,
  String refreshToken,
  long expiraEnSegundos,
  String correo,
  String rol,
  UsuarioResponse usuario
) {}
