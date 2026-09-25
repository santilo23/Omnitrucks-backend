package um.edu.ar.omnitrucks.posicion;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import um.edu.ar.omnitrucks.viaje.EstadoViaje;

public interface PosicionRepository extends JpaRepository<Posicion, Long> {

	Optional<Posicion> findFirstByViajeIdOrderByRegistradoEnDescIdDesc(Long viajeId);

	/**
	 * Recorrido de un viaje, en orden cronológico, acotado a un rango. El
	 * límite evita devolver decenas de miles de puntos en un viaje largo.
	 *
	 * El rango siempre viene con valores concretos: el servicio reemplaza los
	 * extremos que no se indican por bordes abiertos. Escribirlo como
	 * "(:desde is null or ...)" no funciona contra PostgreSQL, que no puede
	 * inferir el tipo de un parámetro que solo aparece en una comparación con
	 * null y falla con "could not determine data type of parameter".
	 */
	List<Posicion> findByViajeIdAndRegistradoEnBetweenOrderByRegistradoEnAscIdAsc(
		Long viajeId, Instant desde, Instant hasta, Limit limite);

	/**
	 * Última posición de cada viaje que está en ruta, en una sola consulta.
	 * Es lo que alimenta el mapa: sin esto habría un pedido por camión.
	 *
	 * Se compara por id y no por registradoEn para no depender de que dos
	 * posiciones no compartan el mismo instante.
	 */
	@Query("""
		select p from Posicion p
		join fetch p.viaje v
		join fetch v.camion
		where v.estado in :estados
		  and p.id = (select max(p2.id) from Posicion p2 where p2.viaje = v)
		order by v.id asc
		""")
	List<Posicion> ultimasDeViajesEnEstados(@Param("estados") Collection<EstadoViaje> estados);

}
