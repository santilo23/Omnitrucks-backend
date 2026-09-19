package um.edu.ar.omnitrucks.camion;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un camión de la flota.
 *
 * Se usan @Getter/@Setter y no @Data: en entidades JPA, el equals/hashCode que
 * genera @Data recorre todos los campos y rompe con las relaciones lazy.
 */
@Entity
@Table(name = "camion")
@Getter
@Setter
@NoArgsConstructor
public class Camion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 10)
	private String patente;

	@Column(nullable = false, length = 40)
	private String marca;

	@Column(nullable = false, length = 40)
	private String modelo;

	private Integer anio;

	@Column(name = "capacidad_kg")
	private Integer capacidadKg;

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void alCrear() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

}
