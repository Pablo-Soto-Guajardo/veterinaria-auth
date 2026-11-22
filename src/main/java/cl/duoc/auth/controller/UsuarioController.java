package cl.duoc.auth.controller;

import cl.duoc.auth.dto.*;
import cl.duoc.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Administración de cuentas y perfil propio")
public class UsuarioController {
  private final UsuarioService service;
  public UsuarioController(UsuarioService service) { this.service = service; }

  @GetMapping("/me")
  @Operation(summary = "Datos del usuario dueño del token")
  public UsuarioResponse perfil(Authentication auth) {
    return service.perfil(auth.getName());
  }

  @PutMapping("/me")
  @Operation(summary = "Actualiza el perfil propio (no permite cambiar el rol)")
  public UsuarioResponse actualizarPerfil(Authentication auth,
      @Valid @RequestBody PerfilRequest request) {
    return service.actualizarPerfil(auth.getName(), request);
  }

  @GetMapping
  @Operation(summary = "Lista todos los usuarios (ADMIN)")
  public List<UsuarioResponse> listar() { return service.listar(); }

  @GetMapping("/{run}")
  @Operation(summary = "Busca un usuario por RUN (ADMIN)")
  public UsuarioResponse buscar(@PathVariable String run) { return service.buscar(run); }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Crea un usuario con cualquier rol (ADMIN)")
  public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
    return service.crear(request);
  }

  @PutMapping("/{run}")
  @Operation(summary = "Actualiza un usuario (ADMIN)")
  public UsuarioResponse actualizar(@PathVariable String run,
      @Valid @RequestBody UsuarioRequest request) {
    return service.actualizar(run, request);
  }

  @DeleteMapping("/{run}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Elimina un usuario (ADMIN)")
  public void eliminar(@PathVariable String run, Authentication auth) {
    service.eliminar(run, auth.getName());
  }
}
