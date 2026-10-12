package um.edu.ar.omnitrucks.chofer.adapters.in.web;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.chofer.application.dto.RutaChoferResponse;
import um.edu.ar.omnitrucks.chofer.application.dto.ViajeChoferResumenResponse;
import um.edu.ar.omnitrucks.chofer.application.port.in.ConsultarRutaChoferUseCase;

/**
 * Adaptador de entrada web (REST) exclusivo para choferes.
 * Expone la ruta y vehículo asignado al chofer en sesión.
 */
@RestController
@RequestMapping("/api/chofer")
public class ChoferRutaController {

	private final ConsultarRutaChoferUseCase rutaUseCase;

	public ChoferRutaController(ConsultarRutaChoferUseCase rutaUseCase) {
		this.rutaUseCase = rutaUseCase;
	}

	/**
	 * Retorna la ruta activa o en curso del chofer con su vehículo y recorrido.
	 * Si no tiene viaje activo devuelve 204 No Content.
	 */
	@GetMapping("/ruta-activa")
	public ResponseEntity<RutaChoferResponse> consultarRutaActiva(Principal principal) {
		return rutaUseCase.consultarRutaActiva(principal.getName())
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	/**
	 * Retorna los detalles de la ruta de un viaje asignado al chofer.
	 */
	@GetMapping("/viajes/{viajeId}/ruta")
	public RutaChoferResponse consultarRutaPorViaje(
			@PathVariable Long viajeId,
			Principal principal) {
		return rutaUseCase.consultarRutaPorViajeId(viajeId, principal.getName());
	}

	/**
	 * Lista todos los viajes asignados al chofer autenticado.
	 */
	@GetMapping("/mis-viajes")
	public List<ViajeChoferResumenResponse> listarMisViajes(Principal principal) {
		return rutaUseCase.listarMisViajes(principal.getName());
	}

}
