package cl.duoc.auth.service;

import cl.duoc.auth.dto.*;
import cl.duoc.auth.exception.ReglaNegocioException;
import cl.duoc.auth.exception.TokenInvalidoException;
import cl.duoc.auth.model.Rol;
import cl.duoc.auth.model.Usuario;
import cl.duoc.auth.repository.UsuarioRepository;
import cl.duoc.auth.security.JwtService;
import cl.duoc.auth.util.Run;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lógica de autenticación: login, registro de clientes, renovación y cierre de sesión.
@Service
public class AuthService {
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final RefreshTokenService refreshTokens;

  public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder,
      JwtService jwt, RefreshTokenService refreshTokens) {
    this.usuarios = usuarios;
    this.encoder = encoder;
    this.jwt = jwt;
    this.refreshTokens = refreshTokens;
  }

  @Transactional
  public LoginResponse login(LoginRequest request) {
    Usuario usuario = usuarios.findByCorreoIgnoreCase(request.correo().trim())
      .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
    if (!encoder.matches(request.password(), usuario.getPassword())) {
      throw new BadCredentialsException("Credenciales inválidas");
    }
    return emitirSesion(usuario);
  }

  // Registro público desde la tienda: siempre crea un CLIENTE.
  @Transactional
  public LoginResponse registrar(RegistroRequest request) {
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
    Usuario usuario = new Usuario();
    usuario.setRun(run);
    usuario.setNombre(request.nombre().trim());
    usuario.setApellidos(request.apellidos().trim());
    usuario.setCorreo(correo);
    usuario.setPassword(encoder.encode(request.password()));
    usuario.setRol(Rol.CLIENTE);
    usuario.setFechaNacimiento(request.fechaNacimiento());
    usuario.setDireccion(request.direccion().trim());
    usuario.setRegion(request.region());
    usuario.setComuna(request.comuna());
    return emitirSesion(usuarios.save(usuario));
  }

  // Renovación: consume el refresh token (rotación) y entrega un par nuevo.
  // El rol se vuelve a leer desde la base, así un cambio de rol se aplica.
  // noRollbackFor: si se detecta un token reutilizado, la revocación de las
  // demás sesiones debe quedar guardada aunque la respuesta sea 401.
  @Transactional(noRollbackFor = TokenInvalidoException.class)
  public LoginResponse refrescar(RefreshRequest request) {
    Usuario usuario = refreshTokens.consumir(request.refreshToken());
    return emitirSesion(usuario);
  }

  @Transactional
  public void cerrarSesion(RefreshRequest request) {
    refreshTokens.revocar(request.refreshToken());
  }

  private LoginResponse emitirSesion(Usuario usuario) {
    return new LoginResponse(
      jwt.generar(usuario),
      refreshTokens.crear(usuario),
      jwt.getExpiracionSegundos(),
      usuario.getCorreo(),
      usuario.getRol().name(),
      UsuarioResponse.desde(usuario));
  }
}