package um.edu.ar.omnitrucks.camion.adapters.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.camion.application.dto.FiltroFlota;
import um.edu.ar.omnitrucks.camion.application.dto.FlotaResumenResponse;
import um.edu.ar.omnitrucks.camion.application.port.in.ConsultarFlotaAdminUseCase;
import um.edu.ar.omnitrucks.camion.dto.CamionResponse;

/**
 * Adaptador de entrada web (REST) exclusivo para el Panel de Administración.
 * Expone la consulta y monitoreo operativo de toda la flota de camiones.
 */
@RestController
@RequestMapping("/api/admin/flota")
public class AdminFlotaController {

	private final ConsultarFlotaAdminUseCase consultarFlotaUseCase;

	public AdminFlotaController(ConsultarFlotaAdminUseCase consultarFlotaUseCase) {
		this.consultarFlotaUseCase = consultarFlotaUseCase;
	}

	/**
	 * Retorna la lista de camiones con su estado operativo según el filtro solicitado
	 * (TODOS, ACTIVOS, INACTIVOS).
	 */
	@GetMapping
	public List<CamionResponse> consultarFlota(
			@RequestParam(required = false, defaultValue = "TODOS") FiltroFlota filtro) {
		return consultarFlotaUseCase.consultarFlota(filtro);
	}

	/**
	 * Retorna las métricas y resumen de estado de la flota para el dashboard de administración.
	 */
	@GetMapping("/resumen")
	public FlotaResumenResponse obtenerResumen() {
		return consultarFlotaUseCase.obtenerResumen();
	}

}
