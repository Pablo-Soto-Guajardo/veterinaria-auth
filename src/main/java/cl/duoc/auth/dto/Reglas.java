package cl.duoc.auth.dto;

public final class Reglas {
  public static final String RUN = "^[0-9]{7,8}[0-9Kk]$";
  public static final String CORREO = "^[^\\s@]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$";
  public static final String MENSAJE_CORREO =
    "solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com";

  private Reglas() {}
}
