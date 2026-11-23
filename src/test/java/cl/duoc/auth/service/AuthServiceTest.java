package cl.duoc.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import cl.duoc.auth.dto.*;
import cl.duoc.auth.exception.ReglaNegocioException;
import cl.duoc.auth.model.Rol;
import cl.duoc.auth.model.Usuario;
import cl.duoc.auth.repository.UsuarioRepository;
import cl.duoc.auth.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock UsuarioRepository usuarios;
  @Mock PasswordEncoder encoder;
  @Mock JwtService jwt;
  @Mock RefreshTokenService refreshTokens;
  @InjectMocks AuthService service;

  Usuario admin;

  @BeforeEach
  void preparar() {
    admin = new Usuario();
    admin.setId(1L);
    admin.setRun("190110222");
    admin.setNombre("Administrador");
    admin.setApellidos("San Marcos");
    admin.setCorreo("admin@duoc.cl");
    admin.setPassword("hash");
    admin.setRol(Rol.ADMIN);
    admin.setDireccion("Av. San Juan 1020");
    admin.setRegion("OHI");
    admin.setComuna("Rancagua");
  }

  @Test
  void loginCorrectoEntregaTokenYRol() {
    when(usuarios.findByCorreoIgnoreCase("admin@duoc.cl")).thenReturn(Optional.of(admin));
    when(encoder.matches("admin123", "hash")).thenReturn(true);
    when(jwt.generar(admin)).thenReturn("jwt-falso");
    when(refreshTokens.crear(admin)).thenReturn("refresh-falso");

    LoginResponse respuesta = service.login(new LoginRequest("admin@duoc.cl", "admin123"));

    assertEquals("jwt-falso", respuesta.token());
    assertEquals("refresh-falso", respuesta.refreshToken());
    assertEquals("ADMIN", respuesta.rol());
    assertEquals("Administrador", respuesta.usuario().nombre());
  }

  @Test
  void loginConContrasenaIncorrectaLanzaError() {
    when(usuarios.findByCorreoIgnoreCase("admin@duoc.cl")).thenReturn(Optional.of(admin));
    when(encoder.matches("otra", "hash")).thenReturn(false);

    assertThrows(BadCredentialsException.class,
      () -> service.login(new LoginRequest("admin@duoc.cl", "otra")));
    verify(jwt, never()).generar(any());
  }

  @Test
  void registroCreaSiempreUnCliente() {
    when(usuarios.existsByRun("187654327")).thenReturn(false);
    when(usuarios.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(false);
    when(encoder.encode("clave1")).thenReturn("hash-clave1");
    when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    RegistroRequest request = new RegistroRequest("187654327", "Ana", "Pérez",
      "ana@gmail.com", "clave1", null, "Calle 1", "RM", "Santiago");
    LoginResponse respuesta = service.registrar(request);

    assertEquals("CLIENTE", respuesta.rol());
    assertEquals("ana@gmail.com", respuesta.correo());
  }

  @Test
  void registroRechazaCorreoRepetido() {
    when(usuarios.existsByRun("187654327")).thenReturn(false);
    when(usuarios.existsByCorreoIgnoreCase("admin@duoc.cl")).thenReturn(true);

    RegistroRequest request = new RegistroRequest("187654327", "Ana", "Pérez",
      "admin@duoc.cl", "clave1", null, "Calle 1", "RM", "Santiago");

    assertThrows(ReglaNegocioException.class, () -> service.registrar(request));
    verify(usuarios, never()).save(any());
  }

  @Test
  void refreshEntregaUnParNuevo() {
    when(refreshTokens.consumir("viejo")).thenReturn(admin);
    when(jwt.generar(admin)).thenReturn("jwt-nuevo");
    when(refreshTokens.crear(admin)).thenReturn("refresh-nuevo");

    LoginResponse respuesta = service.refrescar(new RefreshRequest("viejo"));

    assertEquals("jwt-nuevo", respuesta.token());
    assertEquals("refresh-nuevo", respuesta.refreshToken());
  }
}