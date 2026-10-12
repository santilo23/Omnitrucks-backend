package um.edu.ar.omnitrucks.admin.adapters.in.web;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import um.edu.ar.omnitrucks.admin.application.dto.AsignarViajeAdminRequest;
import um.edu.ar.omnitrucks.admin.application.dto.CrearChoferRequest;
import um.edu.ar.omnitrucks.admin.application.dto.ViajeMonitoreoAdminResponse;
import um.edu.ar.omnitrucks.admin.application.port.in.GestionAdminOperacionesUseCase;
import um.edu.ar.omnitrucks.usuario.dto.UsuarioResponse;

/**
 * Adaptador de entrada web (REST) exclusivo para administradores:
 * - Alta de choferes con contraseñas seguras.
 * - Asignación de viajes e inicio de simulación.
 * - Monitoreo de choferes en conducción y obtención del código de seguimiento para clientes.
 * - Generación de viajes simulados on-demand para pruebas del mapa.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminOperacionesController {

	private final GestionAdminOperacionesUseCase operacionesUseCase;

	public AdminOperacionesController(GestionAdminOperacionesUseCase operacionesUseCase) {
		this.operacionesUseCase = operacionesUseCase;
	}

	@PostMapping("/choferes")
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse crearChofer(@Valid @RequestBody CrearChoferRequest request) {
		return operacionesUseCase.crearChofer(request);
	}

	@GetMapping("/choferes")
	public List<UsuarioResponse> listarChoferes() {
		return operacionesUseCase.listarChoferes();
	}

	@PostMapping("/viajes")
	@ResponseStatus(HttpStatus.CREATED)
	public ViajeMonitoreoAdminResponse asignarViaje(@Valid @RequestBody AsignarViajeAdminRequest request) {
		return operacionesUseCase.asignarViaje(request);
	}

	@GetMapping("/viajes/activos")
	public List<ViajeMonitoreoAdminResponse> listarViajesActivos() {
		return operacionesUseCase.listarViajesActivos();
	}

	@PostMapping("/simulacion/crear-demo")
	@ResponseStatus(HttpStatus.CREATED)
	public ViajeMonitoreoAdminResponse generarViajeSimuladoDemo() {
		return operacionesUseCase.generarViajeSimuladoDemo();
	}

}
