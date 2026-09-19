package um.edu.ar.omnitrucks.camion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.camion.dto.CamionRequest;
import um.edu.ar.omnitrucks.camion.dto.CamionResponse;
import um.edu.ar.omnitrucks.common.error.ConflictoException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;

@Service
@Transactional(readOnly = true)
public class CamionService {

	private final CamionRepository repositorio;

	public CamionService(CamionRepository repositorio) {
		this.repositorio = repositorio;
	}

	public List<CamionResponse> listar(boolean incluirInactivos) {
		List<Camion> camiones = incluirInactivos
			? repositorio.findAllByOrderByPatenteAsc()
			: repositorio.findAllByActivoTrueOrderByPatenteAsc();

		return camiones.stream().map(CamionResponse::desde).toList();
	}

	public CamionResponse obtener(Long id) {
		return CamionResponse.desde(buscar(id));
	}

	@Transactional
	public CamionResponse crear(CamionRequest pedido) {
		String patente = normalizar(pedido.patente());

		if (repositorio.existsByPatente(patente)) {
			throw new ConflictoException("Ya existe un camión con la patente %s.".formatted(patente));
		}

		Camion camion = new Camion();
		camion.setPatente(patente);
		aplicar(pedido, camion);

		return CamionResponse.desde(repositorio.save(camion));
	}

	@Transactional
	public CamionResponse actualizar(Long id, CamionRequest pedido) {
		Camion camion = buscar(id);
		String patente = normalizar(pedido.patente());

		// Solo es conflicto si la patente pasó a ser la de otro camión.
		if (!patente.equals(camion.getPatente()) && repositorio.existsByPatente(patente)) {
			throw new ConflictoException("Ya existe un camión con la patente %s.".formatted(patente));
		}

		camion.setPatente(patente);
		aplicar(pedido, camion);

		return CamionResponse.desde(camion);
	}

	/**
	 * Baja lógica. No se borra el registro porque los viajes ya hechos siguen
	 * apuntando al camión y su historial tiene que seguir siendo consultable.
	 */
	@Transactional
	public void desactivar(Long id) {
		Camion camion = buscar(id);

		if (!camion.isActivo()) {
			throw new ConflictoException("El camión %s ya está dado de baja.".formatted(camion.getPatente()));
		}

		camion.setActivo(false);
	}

	@Transactional
	public CamionResponse reactivar(Long id) {
		Camion camion = buscar(id);
		camion.setActivo(true);
		return CamionResponse.desde(camion);
	}

	private Camion buscar(Long id) {
		return repositorio.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("camión", id));
	}

	private void aplicar(CamionRequest pedido, Camion camion) {
		camion.setMarca(pedido.marca().trim());
		camion.setModelo(pedido.modelo().trim());
		camion.setAnio(pedido.anio());
		camion.setCapacidadKg(pedido.capacidadKg());
	}

	private String normalizar(String patente) {
		return patente.trim().toUpperCase();
	}

}
