package um.edu.ar.omnitrucks.chofer.application.port.in;

import java.util.List;
import java.util.Optional;

import um.edu.ar.omnitrucks.chofer.application.dto.RutaChoferResponse;
import um.edu.ar.omnitrucks.chofer.application.dto.ViajeChoferResumenResponse;

/**
 * Caso de uso para la consulta y seguimiento de rutas asignadas exclusivamente a choferes.
 */
public interface ConsultarRutaChoferUseCase {

	/**
	 * Consulta la ruta actualmente en curso o activa asignada al chofer.
	 */
	Optional<RutaChoferResponse> consultarRutaActiva(String emailChofer);

	/**
	 * Consulta el detalle de una ruta específica por viaje, asegurando que pertenezca al chofer solicitante.
	 */
	RutaChoferResponse consultarRutaPorViajeId(Long viajeId, String emailChofer);

	/**
	 * Lista todos los viajes asignados al chofer.
	 */
	List<ViajeChoferResumenResponse> listarMisViajes(String emailChofer);

}
