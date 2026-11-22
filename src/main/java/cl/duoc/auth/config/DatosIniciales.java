package cl.duoc.auth.config;

import cl.duoc.auth.model.Rol;
import cl.duoc.auth.model.Usuario;
import cl.duoc.auth.repository.UsuarioRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosIniciales {
  @Bean CommandLineRunner usuariosIniciales(UsuarioRepository repo, PasswordEncoder encoder) {
    return args -> {
      crear(repo, encoder, "190110222", "Administrador", "San Marcos", "admin@duoc.cl",
        "admin123", Rol.ADMIN, "1990-04-12", "Av. San Juan 1020", "OHI", "Rancagua");
      crear(repo, encoder, "142234564", "Camila", "Valenzuela Rojas", "vendedor@duoc.cl",
        "vende123", Rol.VENDEDOR, "1995-08-30", "Los Aromos 455", "OHI", "Machalí");
      crear(repo, encoder, "201234565", "Javiera", "Rojas Fuentes", "cliente@gmail.com",
        "cliente1", Rol.CLIENTE, "2000-01-15", "Los Crisantemos 120, Edificio Norte", "RM", "Providencia");
    };
  }

  private void crear(UsuarioRepository repo, PasswordEncoder encoder, String run,
      String nombre, String apellidos, String correo, String password, Rol rol,
      String fecha, String direccion, String region, String comuna) {
    if (repo.findByCorreoIgnoreCase(correo).isPresent()) return;
    Usuario u = new Usuario();
    u.setRun(run);
    u.setNombre(nombre);
    u.setApellidos(apellidos);
    u.setCorreo(correo);
    u.setPassword(encoder.encode(password));
    u.setRol(rol);
    u.setFechaNacimiento(LocalDate.parse(fecha));
    u.setDireccion(direccion);
    u.setRegion(region);
    u.setComuna(comuna);
    repo.save(u);
  }
}
