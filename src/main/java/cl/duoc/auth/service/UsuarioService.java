package cl.duoc.auth.service;

import cl.duoc.auth.dto.*;
import cl.duoc.auth.exception.RecursoNoEncontradoException;
import cl.duoc.auth.exception.ReglaNegocioException;
import cl.duoc.auth.model.Rol;
import cl.duoc.auth.model.Usuario;
import cl.duoc.auth.repository.UsuarioRepository;
import cl.duoc.auth.util.Run;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;
  private final RefreshTokenService refreshTokens;

  public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder,
      RefreshTokenService refreshTokens) {
    this.usuarios = usuarios;
    this.encoder = encoder;
    this.refreshTokens = refreshTokens;
  }

  @Transactional(readOnly = true)
  public List<UsuarioResponse> listar() {
    return usuarios.findAll(Sort.by("nombre", "apellidos")).stream()
      .map(UsuarioResponse::desde).toList();
  }

  @Transactional(readOnly = true)
  public UsuarioResponse buscar(String run) {
    return UsuarioResponse.desde(buscarEntidad(run));
  }

  @Transactional
  public UsuarioResponse crear(UsuarioRequest request) {
    String run = Run.normalizar(request.run());
    String correo = request.correo().trim().toLowerCase();
    if (!Run.esValido(run)) {
      throw new ReglaNegocioException("El dígito verificador del RUN no es correcto");
    }
    if (usuarios.existsByRun(run)) {
      throw new ReglaNegocioException("Ya existe un usuario registrado con ese RUN");
    }
    if (usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ReglaNegocioException("Ya existe un usuario registrado con ese correo");
    }
    if (request.password() == null || request.password().isBlank()) {
      throw new ReglaNegocioException("La contraseña es obligatoria");
    }
    Usuario usuario = new Usuario();
    usuario.setRun(run);
    usuario.setCorreo(correo);
    usuario.setRol(request.rol());
    usuario.setPassword(encoder.encode(validarPassword(request.password())));
    copiarDatos(usuario, request.nombre(), request.apellidos(), request.fechaNacimiento(),
      request.direccion(), request.region(), request.comuna());
    return UsuarioResponse.desde(usuarios.save(usuario));
  }

  @Transactional
  public UsuarioResponse actualizar(String run, UsuarioRequest request) {
    Usuario usuario = buscarEntidad(run);
    String correo = request.correo().trim().toLowerCase();
    usuarios.findByCorreoIgnoreCase(correo)
      .filter(otro -> !otro.getId().equals(usuario.getId()))
      .ifPresent(otro -> {
        throw new ReglaNegocioException("Ese correo ya está en uso por otro usuario");
      });
    if (usuario.getRol() == Rol.ADMIN && request.rol() != Rol.ADMIN
        && usuarios.countByRol(Rol.ADMIN) == 1) {
      throw new ReglaNegocioException("No se puede quitar el rol al único administrador");
    }
    boolean cambioRol = usuario.getRol() != request.rol();
    usuario.setCorreo(correo);
    usuario.setRol(request.rol());
    if (request.password() != null && !request.password().isBlank()) {
      usuario.setPassword(encoder.encode(validarPassword(request.password())));
    }
    copiarDatos(usuario, request.nombre(), request.apellidos(), request.fechaNacimiento(),
      request.direccion(), request.region(), request.comuna());
    // Si cambian sus permisos, sus sesiones abiertas dejan de poder renovarse.
    if (cambioRol) {
      refreshTokens.revocarTodos(usuario);
    }
    return UsuarioResponse.desde(usuario);
  }

  @Transactional
  public void eliminar(String run, String correoSolicitante) {
    Usuario usuario = buscarEntidad(run);
    if (usuario.getCorreo().equalsIgnoreCase(correoSolicitante)) {
      throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
    }
    if (usuario.getRol() == Rol.ADMIN && usuarios.countByRol(Rol.ADMIN) == 1) {
      throw new ReglaNegocioException("No se puede eliminar al único administrador");
    }
    refreshTokens.eliminarDeUsuario(usuario);
    usuarios.delete(usuario);
  }

  @Transactional(readOnly = true)
  public UsuarioResponse perfil(String correo) {
    return UsuarioResponse.desde(buscarPorCorreo(correo));
  }

  @Transactional
  public UsuarioResponse actualizarPerfil(String correo, PerfilRequest request) {
    Usuario usuario = buscarPorCorreo(correo);
    if (request.password() != null && !request.password().isBlank()) {
      usuario.setPassword(encoder.encode(validarPassword(request.password())));
    }
    copiarDatos(usuario, request.nombre(), request.apellidos(), request.fechaNacimiento(),
      request.direccion(), request.region(), request.comuna());
    return UsuarioResponse.desde(usuario);
  }

  private Usuario buscarEntidad(String run) {
    return usuarios.findByRun(Run.normalizar(run))
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + run));
  }

  private Usuario buscarPorCorreo(String correo) {
    return usuarios.findByCorreoIgnoreCase(correo)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + correo));
  }

  private String validarPassword(String password) {
    if (password.length() < 4 || password.length() > 10) {
      throw new ReglaNegocioException("La contraseña debe tener entre 4 y 10 caracteres");
    }
    return password;
  }

  private void copiarDatos(Usuario usuario, String nombre, String apellidos,
      java.time.LocalDate fechaNacimiento, String direccion, String region, String comuna) {
    usuario.setNombre(nombre.trim());
    usuario.setApellidos(apellidos.trim());
    usuario.setFechaNacimiento(fechaNacimiento);
    usuario.setDireccion(direccion.trim());
    usuario.setRegion(region);
    usuario.setComuna(comuna);
  }
}