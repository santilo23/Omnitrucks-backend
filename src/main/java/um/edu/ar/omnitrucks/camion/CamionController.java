package um.edu.ar.omnitrucks.camion;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.camion.dto.CamionRequest;
import um.edu.ar.omnitrucks.camion.dto.CamionResponse;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

	private final CamionService servicio;

	public CamionController(CamionService servicio) {
		this.servicio = servicio;
	}

	/**
	 * Lista los camiones. Por defecto solo los activos; con
	 * {@code ?incluirInactivos=true} también los dados de baja.
	 */
	@GetMapping
	public List<CamionResponse> listar(
			@RequestParam(defaultValue = "false") boolean incluirInactivos) {
		return servicio.listar(incluirInactivos);
	}

	@GetMapping("/{id}")
	public CamionResponse obtener(@PathVariable Long id) {
		return servicio.obtener(id);
	}

	@PostMapping
	public ResponseEntity<CamionResponse> crear(@Valid @RequestBody CamionRequest pedido) {
		CamionResponse creado = servicio.crear(pedido);
		return ResponseEntity
			.created(URI.create("/api/camiones/" + creado.id()))
			.body(creado);
	}

	@PutMapping("/{id}")
	public CamionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CamionRequest pedido) {
		return servicio.actualizar(id, pedido);
	}

	/** Baja lógica: el camión deja de aparecer en los listados pero no se borra. */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> desactivar(@PathVariable Long id) {
		servicio.desactivar(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/reactivar")
	public CamionResponse reactivar(@PathVariable Long id) {
		return servicio.reactivar(id);
	}

}
