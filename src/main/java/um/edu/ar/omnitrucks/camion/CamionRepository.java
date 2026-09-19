package um.edu.ar.omnitrucks.camion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CamionRepository extends JpaRepository<Camion, Long> {

	Optional<Camion> findByPatente(String patente);

	boolean existsByPatente(String patente);

	List<Camion> findAllByActivoTrueOrderByPatenteAsc();

	List<Camion> findAllByOrderByPatenteAsc();

}
