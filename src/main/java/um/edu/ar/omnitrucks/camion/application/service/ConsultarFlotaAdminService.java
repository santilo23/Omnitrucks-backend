package um.edu.ar.omnitrucks.camion.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.application.dto.FiltroFlota;
import um.edu.ar.omnitrucks.camion.application.dto.FlotaResumenResponse;
import um.edu.ar.omnitrucks.camion.application.port.in.ConsultarFlotaAdminUseCase;
import um.edu.ar.omnitrucks.camion.domain.model.EstadoOperativoCamion;
import um.edu.ar.omnitrucks.camion.domain.port.CamionRepositoryPort;
import um.edu.ar.omnitrucks.camion.domain.strategy.DeterminadorEstadoCamionStrategy;
import um.edu.ar.omnitrucks.camion.dto.CamionResponse;

/**
 * Caso de uso para el panel de administración que gestiona y consulta la flota completa,
 * determinando sus estados operativos y contadores globales.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarFlotaAdminService implements ConsultarFlotaAdminUseCase {

	private final CamionRepositoryPort camionRepository;
	private final DeterminadorEstadoCamionStrategy strategy;

	public ConsultarFlotaAdminService(
			CamionRepositoryPort camionRepository,
			DeterminadorEstadoCamionStrategy strategy) {
		this.camionRepository = camionRepository;
		this.strategy = strategy;
	}

	@Override
	public List<CamionResponse> consultarFlota(FiltroFlota filtro) {
		FiltroFlota criterio = (filtro != null) ? filtro : FiltroFlota.TODOS;

		List<Camion> camiones = switch (criterio) {
			case ACTIVOS -> camionRepository.listarActivosOrdenadosPorPatente();
			case INACTIVOS -> camionRepository.listarInactivosOrdenadosPorPatente();
			case TODOS -> camionRepository.listarTodosOrdenadosPorPatente();
		};

		return camiones.stream()
				.map(c -> CamionResponse.desde(c, strategy.determinarEstado(c)))
				.toList();
	}

	@Override
	public FlotaResumenResponse obtenerResumen() {
		long total = camionRepository.contarTotal();
		long activos = camionRepository.contarActivos();
		long inactivos = camionRepository.contarInactivos();

		List<Camion> camionesActivos = camionRepository.listarActivosOrdenadosPorPatente();
		long enTransito = camionesActivos.stream()
				.filter(c -> strategy.determinarEstado(c) == EstadoOperativoCamion.EN_TRANSITO)
				.count();
		long disponibles = activos - enTransito;

		return new FlotaResumenResponse(total, activos, inactivos, disponibles, enTransito);
	}

}
