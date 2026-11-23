package cl.duoc.auth.repository;

import cl.duoc.auth.model.Rol;
import cl.duoc.auth.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByCorreoIgnoreCase(String correo);
  Optional<Usuario> findByRun(String run);
  boolean existsByCorreoIgnoreCase(String correo);
  boolean existsByRun(String run);
  long countByRol(Rol rol);
}