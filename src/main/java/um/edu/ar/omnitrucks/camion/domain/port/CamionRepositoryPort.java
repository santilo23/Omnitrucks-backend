package um.edu.ar.omnitrucks.camion.domain.port;

import java.util.List;
import java.util.Optional;

import um.edu.ar.omnitrucks.camion.Camion;

/**
 * Puerto de dominio que abstrae las operaciones de persistencia de la flota.
 */
public interface CamionRepositoryPort {

	List<Camion> listarTodosOrdenadosPorPatente();

	List<Camion> listarActivosOrdenadosPorPatente();

	List<Camion> listarInactivosOrdenadosPorPatente();

	Optional<Camion> buscarPorId(Long id);

	Optional<Camion> buscarPorPatente(String patente);

	boolean existePorPatente(String patente);

	Camion guardar(Camion camion);

	long contarTotal();

	long contarActivos();

	long contarInactivos();

}
