package um.edu.ar.omnitrucks.camion.application.port.in;

import java.util.List;

import um.edu.ar.omnitrucks.camion.application.dto.FiltroFlota;
import um.edu.ar.omnitrucks.camion.application.dto.FlotaResumenResponse;
import um.edu.ar.omnitrucks.camion.dto.CamionResponse;

/**
 * Caso de uso para la administración integral de la flota de camiones.
 */
public interface ConsultarFlotaAdminUseCase {

	List<CamionResponse> consultarFlota(FiltroFlota filtro);

	FlotaResumenResponse obtenerResumen();

}
