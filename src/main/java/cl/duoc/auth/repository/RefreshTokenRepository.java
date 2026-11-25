package cl.duoc.auth.repository;

import cl.duoc.auth.model.RefreshToken;
import cl.duoc.auth.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);
  List<RefreshToken> findByUsuarioAndRevocadoFalse(Usuario usuario);
  void deleteByUsuario(Usuario usuario);
}