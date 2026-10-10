package um.edu.ar.omnitrucks.camion.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.camion.domain.port.CamionRepositoryPort;

/**
 * Adaptador de persistencia que conecta el puerto CamionRepositoryPort con Spring Data JPA.
 */
@Component
public class CamionRepositoryAdapter implements CamionRepositoryPort {

	private final CamionRepository springDataRepository;

	public CamionRepositoryAdapter(CamionRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public List<Camion> listarTodosOrdenadosPorPatente() {
		return springDataRepository.findAllByOrderByPatenteAsc();
	}

	@Override
	public List<Camion> listarActivosOrdenadosPorPatente() {
		return springDataRepository.findAllByActivoTrueOrderByPatenteAsc();
	}

	@Override
	public List<Camion> listarInactivosOrdenadosPorPatente() {
		return springDataRepository.findAllByActivoFalseOrderByPatenteAsc();
	}

	@Override
	public Optional<Camion> buscarPorId(Long id) {
		return springDataRepository.findById(id);
	}

	@Override
	public Optional<Camion> buscarPorPatente(String patente) {
		return springDataRepository.findByPatente(patente);
	}

	@Override
	public boolean existePorPatente(String patente) {
		return springDataRepository.existsByPatente(patente);
	}

	@Override
	public Camion guardar(Camion camion) {
		return springDataRepository.save(camion);
	}

	@Override
	public long contarTotal() {
		return springDataRepository.count();
	}

	@Override
	public long contarActivos() {
		return springDataRepository.countByActivoTrue();
	}

	@Override
	public long contarInactivos() {
		return springDataRepository.countByActivoFalse();
	}

}
