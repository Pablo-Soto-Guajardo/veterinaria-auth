package cl.duoc.auth.controller;

import cl.duoc.auth.dto.*;
import cl.duoc.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Endpoints públicos de autenticación. Versión 1 de la API.
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación del JWT")
@SecurityRequirements // en Swagger se muestran sin candado: no requieren token
public class AuthController {
  private final AuthService auth;
  public AuthController(AuthService auth) { this.auth = auth; }

  @PostMapping("/login")
  @Operation(summary = "Inicia sesión y entrega un JWT y un refresh token")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return auth.login(request);
  }

  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Registra un cliente nuevo e inicia su sesión")
  public LoginResponse registrar(@Valid @RequestBody RegistroRequest request) {
    return auth.registrar(request);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Renueva el JWT usando un refresh token (de un solo uso)")
  public LoginResponse refrescar(@Valid @RequestBody RefreshRequest request) {
    return auth.refrescar(request);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Cierra la sesión revocando el refresh token")
  public void cerrarSesion(@Valid @RequestBody RefreshRequest request) {
    auth.cerrarSesion(request);
  }
}