package cl.duoc.auth.exception;


public class TokenInvalidoException extends RuntimeException {
  public TokenInvalidoException(String mensaje) {
    super(mensaje);
  }
}