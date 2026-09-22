package um.edu.ar.omnitrucks.viaje;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.viaje.dto.ViajeRequest;
import um.edu.ar.omnitrucks.viaje.dto.ViajeResponse;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

	private final ViajeService servicio;

	public ViajeController(ViajeService servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public List<ViajeResponse> listar(@RequestParam(required = false) EstadoViaje estado) {
		return servicio.listar(estado);
	}

	/** Viajes en la ruta ahora mismo. Es el endpoint que consume el mapa. */
	@GetMapping("/activos")
	public List<ViajeResponse> activos() {
		return servicio.activos();
	}

	@GetMapping("/{id}")
	public ViajeResponse obtener(@PathVariable Long id) {
		return servicio.obtener(id);
	}

	@PostMapping
	public ResponseEntity<ViajeResponse> crear(@Valid @RequestBody ViajeRequest pedido) {
		ViajeResponse creado = servicio.crear(pedido);
		return ResponseEntity
			.created(URI.create("/api/viajes/" + creado.id()))
			.body(creado);
	}

	@PostMapping("/{id}/iniciar")
	public ViajeResponse iniciar(@PathVariable Long id) {
		return servicio.iniciar(id);
	}

	@PostMapping("/{id}/pausar")
	public ViajeResponse pausar(@PathVariable Long id) {
		return servicio.pausar(id);
	}

	@PostMapping("/{id}/reanudar")
	public ViajeResponse reanudar(@PathVariable Long id) {
		return servicio.reanudar(id);
	}

	@PostMapping("/{id}/finalizar")
	public ViajeResponse finalizar(@PathVariable Long id) {
		return servicio.finalizar(id);
	}

	@PostMapping("/{id}/cancelar")
	public ViajeResponse cancelar(@PathVariable Long id) {
		return servicio.cancelar(id);
	}

}
