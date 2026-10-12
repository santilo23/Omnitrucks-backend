package um.edu.ar.omnitrucks.viaje.application.port.in;

import java.util.List;

import um.edu.ar.omnitrucks.viaje.application.dto.EnvioClienteResumenResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.SeguimientoClienteResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.UbicacionSeguimientoResponse;

/**
 * Caso de uso para la consulta segura de envíos y mapa por parte de clientes.
 */
public interface ConsultarSeguimientoClienteUseCase {

	SeguimientoClienteResponse consultarPorCodigo(String codigoSeguimiento, String emailUsuarioAutenticado);

	List<EnvioClienteResumenResponse> listarEnviosCliente(String emailUsuarioAutenticado);

	UbicacionSeguimientoResponse consultarUbicacionEnVivo(String codigoSeguimiento, String emailUsuarioAutenticado);

}
