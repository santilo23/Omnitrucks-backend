package um.edu.ar.omnitrucks.admin.application.port.in;

import java.util.List;

import um.edu.ar.omnitrucks.admin.application.dto.AsignarViajeAdminRequest;
import um.edu.ar.omnitrucks.admin.application.dto.CrearChoferRequest;
import um.edu.ar.omnitrucks.admin.application.dto.ViajeMonitoreoAdminResponse;
import um.edu.ar.omnitrucks.usuario.dto.UsuarioResponse;

/**
 * Puerto de entrada (Use Case) para las operaciones del administrador:
 * - Alta de choferes con credenciales seguras.
 * - Asignación de viajes e inicio de simulación.
 * - Monitoreo de viajes con código de seguimiento para clientes.
 * - Generación de viajes de prueba simulados.
 */
public interface GestionAdminOperacionesUseCase {

	UsuarioResponse crearChofer(CrearChoferRequest request);

	List<UsuarioResponse> listarChoferes();

	ViajeMonitoreoAdminResponse asignarViaje(AsignarViajeAdminRequest request);

	List<ViajeMonitoreoAdminResponse> listarViajesActivos();

	ViajeMonitoreoAdminResponse generarViajeSimuladoDemo();

}
