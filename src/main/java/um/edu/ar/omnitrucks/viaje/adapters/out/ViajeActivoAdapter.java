package um.edu.ar.omnitrucks.viaje.adapters.out;

import java.util.List;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.camion.domain.port.VerificadorViajesActivosPort;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Adaptador que conecta el puerto VerificadorViajesActivosPort con el repositorio
 * de viajes para consultar si un camión está actualmente en tránsito.
 */
@Component
public class ViajeActivoAdapter implements VerificadorViajesActivosPort {

	private final ViajeRepository viajeRepository;

	public ViajeActivoAdapter(ViajeRepository viajeRepository) {
		this.viajeRepository = viajeRepository;
	}

	@Override
	public boolean camionTieneViajeActivo(Long camionId) {
		return viajeRepository.existsByCamionIdAndEstadoIn(
				camionId,
				List.of(EstadoViaje.EN_CURSO, EstadoViaje.PAUSADO)
		);
	}

}
