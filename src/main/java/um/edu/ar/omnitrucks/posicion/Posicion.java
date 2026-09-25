package um.edu.ar.omnitrucks.posicion;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * Una coordenada GPS reportada durante un viaje.
 *
 * Es la tabla que más crece: un viaje interprovincial de doce horas reportando
 * cada cinco segundos deja unas 8.600 filas.
 */
@Entity
@Table(name = "posicion")
@Getter
@Setter
@NoArgsConstructor
public class Posicion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "viaje_id", nullable = false)
	private Viaje viaje;

	@Column(nullable = false)
	private double latitud;

	@Column(nullable = false)
	private double longitud;

	@Column(name = "velocidad_kmh")
	private Double velocidadKmh;

	@Column(name = "rumbo_grados")
	private Double rumboGrados;

	@Column(name = "precision_m")
	private Double precisionM;

	/** Momento en el dispositivo que tomó la coordenada. */
	@Column(name = "registrado_en", nullable = false)
	private Instant registradoEn;

	/**
	 * Momento en que el servidor la recibió. Comparado con registradoEn permite
	 * detectar posiciones que llegaron tarde por falta de señal.
	 */
	@Column(name = "recibido_en", nullable = false, updatable = false)
	private Instant recibidoEn;

	@PrePersist
	void alRecibir() {
		if (recibidoEn == null) {
			recibidoEn = Instant.now();
		}
	}

}
