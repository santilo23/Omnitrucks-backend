package um.edu.ar.omnitrucks.usuario.domain.port;

import java.util.List;
import java.util.Optional;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

/**
 * Puerto de dominio que abstrae las operaciones de persistencia sobre la entidad Usuario.
 */
public interface UsuarioRepositoryPort {

	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findById(Long id);

	boolean existsByEmail(String email);

	List<Usuario> findAllByRolAndActivoTrue(Rol rol);

	Usuario save(Usuario usuario);

	long count();

	void deleteAllInBatch();

}
