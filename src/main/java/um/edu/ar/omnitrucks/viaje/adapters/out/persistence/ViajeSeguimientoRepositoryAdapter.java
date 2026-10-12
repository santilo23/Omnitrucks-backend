package um.edu.ar.omnitrucks.viaje.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;
import um.edu.ar.omnitrucks.viaje.domain.port.ViajeSeguimientoRepositoryPort;

/**
 * Adaptador de persistencia para el puerto de seguimiento de viajes.
 */
@Component
public class ViajeSeguimientoRepositoryAdapter implements ViajeSeguimientoRepositoryPort {

	private final ViajeRepository viajeRepository;
	private final PosicionRepository posicionRepository;

	public ViajeSeguimientoRepositoryAdapter(
			ViajeRepository viajeRepository,
			PosicionRepository posicionRepository) {
		this.viajeRepository = viajeRepository;
		this.posicionRepository = posicionRepository;
	}

	@Override
	public Optional<Viaje> buscarPorCodigoSeguimiento(String codigoSeguimiento) {
		if (codigoSeguimiento == null || codigoSeguimiento.isBlank()) {
			return Optional.empty();
		}
		return viajeRepository.findByCodigoSeguimiento(codigoSeguimiento.trim().toUpperCase());
	}

	@Override
	public List<Viaje> listarPorClienteId(Long clienteId) {
		return viajeRepository.findAllByClienteIdOrderByCreatedAtDesc(clienteId);
	}

	@Override
	public Optional<Posicion> buscarUltimaPosicion(Long viajeId) {
		return posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(viajeId);
	}

}
