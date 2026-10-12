package um.edu.ar.omnitrucks.viaje;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViajeRepository extends JpaRepository<Viaje, Long> {

	/**
	 * Se traen camión y chofer en la misma consulta: los listados los muestran
	 * siempre y, al ser relaciones lazy, sin esto habría una consulta por viaje.
	 */
	@EntityGraph(attributePaths = { "camion", "chofer" })
	List<Viaje> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = { "camion", "chofer" })
	List<Viaje> findAllByEstadoOrderByCreatedAtDesc(EstadoViaje estado);

	@EntityGraph(attributePaths = { "camion", "chofer" })
	List<Viaje> findAllByEstado(EstadoViaje estado);

	@EntityGraph(attributePaths = { "camion", "chofer" })
	List<Viaje> findAllByEstadoInOrderByCreatedAtDesc(Collection<EstadoViaje> estados);

	boolean existsByCamionIdAndEstadoIn(Long camionId, Collection<EstadoViaje> estados);

	@EntityGraph(attributePaths = { "camion", "chofer", "cliente" })
	java.util.Optional<Viaje> findByCodigoSeguimiento(String codigoSeguimiento);

	@EntityGraph(attributePaths = { "camion", "chofer", "cliente" })
	List<Viaje> findAllByClienteIdOrderByCreatedAtDesc(Long clienteId);

	@EntityGraph(attributePaths = { "camion", "chofer" })
	java.util.Optional<Viaje> findFirstByChoferIdAndEstadoInOrderByCreatedAtDesc(
			Long choferId, Collection<EstadoViaje> estados);

	@EntityGraph(attributePaths = { "camion", "chofer" })
	List<Viaje> findAllByChoferIdOrderByCreatedAtDesc(Long choferId);

}
