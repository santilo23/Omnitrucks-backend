package um.edu.ar.omnitrucks.viaje.domain.port;

import java.util.List;
import java.util.Optional;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * Puerto de dominio para la consulta de información de seguimiento y envíos asignados a clientes.
 */
public interface ViajeSeguimientoRepositoryPort {

	Optional<Viaje> buscarPorCodigoSeguimiento(String codigoSeguimiento);

	List<Viaje> listarPorClienteId(Long clienteId);

	Optional<Posicion> buscarUltimaPosicion(Long viajeId);

}
