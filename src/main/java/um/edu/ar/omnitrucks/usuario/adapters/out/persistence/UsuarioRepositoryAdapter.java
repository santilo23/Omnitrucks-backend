package um.edu.ar.omnitrucks.usuario.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;

/**
 * Adaptador de persistencia que conecta el puerto UsuarioRepositoryPort
 * con el repositorio Spring Data JPA.
 */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

	private final UsuarioRepository springDataRepository;

	public UsuarioRepositoryAdapter(UsuarioRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Optional<Usuario> findByEmail(String email) {
		return springDataRepository.findByEmail(email);
	}

	@Override
	public Optional<Usuario> findById(Long id) {
		return springDataRepository.findById(id);
	}

	@Override
	public boolean existsByEmail(String email) {
		return springDataRepository.existsByEmail(email);
	}

	@Override
	public List<Usuario> findAllByRolAndActivoTrue(Rol rol) {
		return springDataRepository.findAllByRolAndActivoTrue(rol);
	}

	@Override
	public Usuario save(Usuario usuario) {
		return springDataRepository.save(usuario);
	}

	@Override
	public long count() {
		return springDataRepository.count();
	}

	@Override
	public void deleteAllInBatch() {
		springDataRepository.deleteAllInBatch();
	}

}
