package um.edu.ar.omnitrucks.viaje.adapters.in.web;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.viaje.application.dto.EnvioClienteResumenResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.SeguimientoClienteResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.UbicacionSeguimientoResponse;
import um.edu.ar.omnitrucks.viaje.application.port.in.ConsultarSeguimientoClienteUseCase;

/**
 * Adaptador de entrada web (REST) exclusivo para clientes y seguimiento de envíos.
 */
@RestController
@RequestMapping("/api/cliente")
public class ClienteSeguimientoController {

	private final ConsultarSeguimientoClienteUseCase seguimientoUseCase;

	public ClienteSeguimientoController(ConsultarSeguimientoClienteUseCase seguimientoUseCase) {
		this.seguimientoUseCase = seguimientoUseCase;
	}

	/**
	 * Consulta la información pública y estado de un envío mediante su código único de seguimiento.
	 */
	@GetMapping("/seguimiento/{codigoSeguimiento}")
	public SeguimientoClienteResponse consultarPorCodigo(
			@PathVariable String codigoSeguimiento,
			Principal principal) {
		return seguimientoUseCase.consultarPorCodigo(codigoSeguimiento, principal.getName());
	}

	/**
	 * Consulta la posición en tiempo real del vehículo asignado para renderizar el mapa del cliente.
	 */
	@GetMapping("/seguimiento/{codigoSeguimiento}/ubicacion")
	public UbicacionSeguimientoResponse consultarUbicacionEnVivo(
			@PathVariable String codigoSeguimiento,
			Principal principal) {
		return seguimientoUseCase.consultarUbicacionEnVivo(codigoSeguimiento, principal.getName());
	}

	/**
	 * Lista todos los envíos asociados a la cuenta del cliente autenticado.
	 */
	@GetMapping("/mis-envios")
	public List<EnvioClienteResumenResponse> listarMisEnvios(Principal principal) {
		return seguimientoUseCase.listarEnviosCliente(principal.getName());
	}

}
