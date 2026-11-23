package cl.duoc.auth.exception;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<Map<String, String>> credencialesInvalidas() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(Map.of("error", "Correo o contraseña incorrectos"));
  }

  @ExceptionHandler(TokenInvalidoException.class)
  ResponseEntity<Map<String, String>> tokenInvalido(TokenInvalidoException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(RecursoNoEncontradoException.class)
  ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(ReglaNegocioException.class)
  ResponseEntity<Map<String, String>> reglaNegocio(ReglaNegocioException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
      .body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, String>> datosInvalidos(MethodArgumentNotValidException e) {
    String mensaje = e.getBindingResult().getFieldErrors().stream()
      .findFirst().map(error -> error.getField() + ": " + error.getDefaultMessage())
      .orElse("Datos inválidos");
    return ResponseEntity.badRequest().body(Map.of("error", mensaje));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<Map<String, String>> jsonInvalido() {
    return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo de la solicitud no es válido"));
  }
}