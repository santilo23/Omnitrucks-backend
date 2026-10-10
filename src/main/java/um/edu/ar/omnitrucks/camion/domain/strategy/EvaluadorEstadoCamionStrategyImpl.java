package um.edu.ar.omnitrucks.camion.domain.strategy;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.domain.model.EstadoOperativoCamion;
import um.edu.ar.omnitrucks.camion.domain.port.VerificadorViajesActivosPort;

/**
 * Implementación de la estrategia de determinación de estado operativo de camiones.
 */
@Component
public class EvaluadorEstadoCamionStrategyImpl implements DeterminadorEstadoCamionStrategy {

	private final VerificadorViajesActivosPort verificadorViajesActivos;

	public EvaluadorEstadoCamionStrategyImpl(VerificadorViajesActivosPort verificadorViajesActivos) {
		this.verificadorViajesActivos = verificadorViajesActivos;
	}

	@Override
	public EstadoOperativoCamion determinarEstado(Camion camion) {
		if (camion == null || !camion.isActivo()) {
			return EstadoOperativoCamion.INACTIVO;
		}

		if (camion.getId() != null && verificadorViajesActivos.camionTieneViajeActivo(camion.getId())) {
			return EstadoOperativoCamion.EN_TRANSITO;
		}

		return EstadoOperativoCamion.DISPONIBLE;
	}

}
