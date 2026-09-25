package um.edu.ar.omnitrucks.posicion;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.posicion.dto.PosicionRequest;
import um.edu.ar.omnitrucks.posicion.dto.PosicionResponse;
import um.edu.ar.omnitrucks.posicion.dto.UbicacionActivaResponse;

@RestController
@RequestMapping("/api/viajes")
public class PosicionController {

	private final PosicionService servicio;

	public PosicionController(PosicionService servicio) {
		this.servicio = servicio;
	}

	/**
	 * Registra posiciones en un viaje en ruta.
	 *
	 * Acepta tanto un objeto suelto como un arreglo: la configuración de
	 * Jackson convierte el valor único en una lista de un elemento.
	 */
	@PostMapping("/{viajeId}/posiciones")
	@ResponseStatus(HttpStatus.CREATED)
	public List<PosicionResponse> registrar(
			@PathVariable Long viajeId,
			@Valid @RequestBody List<PosicionRequest> posiciones) {
		return servicio.registrar(viajeId, posiciones);
	}

	@GetMapping("/{viajeId}/posiciones/ultima")
	public PosicionResponse ultima(@PathVariable Long viajeId) {
		return servicio.ultima(viajeId);
	}

	/** Recorrido histórico, en orden cronológico, para dibujar la traza en el mapa. */
	@GetMapping("/{viajeId}/recorrido")
	public List<PosicionResponse> recorrido(
			@PathVariable Long viajeId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
			@RequestParam(required = false) Integer limite) {
		return servicio.recorrido(viajeId, desde, hasta, limite);
	}

	/**
	 * Dónde está cada camión en ruta, en un solo pedido. Evita que la app tenga
	 * que preguntar viaje por viaje para dibujar el mapa.
	 */
	@GetMapping("/activos/posiciones")
	public List<UbicacionActivaResponse> ubicacionesActivas() {
		return servicio.ubicacionesActivas();
	}

}
