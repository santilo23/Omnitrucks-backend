package um.edu.ar.omnitrucks.chofer.domain.port;

import java.util.List;
import java.util.Optional;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * Puerto de dominio para la persistencia y consulta de rutas y viajes asignados a choferes.
 */
public interface ChoferRutaRepositoryPort {

	Optional<Viaje> buscarViajeActivoPorChoferId(Long choferId);

	Optional<Viaje> buscarPorId(Long viajeId);

	List<Viaje> listarViajesPorChoferId(Long choferId);

	Optional<Posicion> buscarUltimaPosicion(Long viajeId);

	List<Posicion> obtenerRecorrido(Long viajeId);

}
