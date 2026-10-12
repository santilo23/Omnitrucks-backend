package um.edu.ar.omnitrucks.chofer.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.chofer.domain.port.ChoferRutaRepositoryPort;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Adaptador de persistencia que implementa el puerto de rutas de choferes.
 */
@Component
public class ChoferRutaRepositoryAdapter implements ChoferRutaRepositoryPort {

	private static final List<EstadoViaje> ESTADOS_ACTIVOS = List.of(
			EstadoViaje.EN_CURSO,
			EstadoViaje.PAUSADO,
			EstadoViaje.PROGRAMADO
	);

	private final ViajeRepository viajeRepository;
	private final PosicionRepository posicionRepository;

	public ChoferRutaRepositoryAdapter(
			ViajeRepository viajeRepository,
			PosicionRepository posicionRepository) {
		this.viajeRepository = viajeRepository;
		this.posicionRepository = posicionRepository;
	}

	@Override
	public Optional<Viaje> buscarViajeActivoPorChoferId(Long choferId) {
		return viajeRepository.findFirstByChoferIdAndEstadoInOrderByCreatedAtDesc(choferId, ESTADOS_ACTIVOS);
	}

	@Override
	public Optional<Viaje> buscarPorId(Long viajeId) {
		return viajeRepository.findById(viajeId);
	}

	@Override
	public List<Viaje> listarViajesPorChoferId(Long choferId) {
		return viajeRepository.findAllByChoferIdOrderByCreatedAtDesc(choferId);
	}

	@Override
	public Optional<Posicion> buscarUltimaPosicion(Long viajeId) {
		return posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(viajeId);
	}

	@Override
	public List<Posicion> obtenerRecorrido(Long viajeId) {
		return posicionRepository.findTop500ByViajeIdOrderByRegistradoEnAscIdAsc(viajeId);
	}

}
