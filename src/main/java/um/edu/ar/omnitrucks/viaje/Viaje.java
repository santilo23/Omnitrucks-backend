package um.edu.ar.omnitrucks.viaje;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.usuario.Usuario;

/**
 * Un traslado de mercadería entre un origen y un destino.
 *
 * Las transiciones de estado no se hacen acá: viven en ViajeService, que es
 * quien valida que sean válidas.
 */
@Entity
@Table(name = "viaje")
@Getter
@Setter
@NoArgsConstructor
public class Viaje {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "camion_id", nullable = false)
	private Camion camion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chofer_id", nullable = false)
	private Usuario chofer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id")
	private Usuario cliente;

	@Column(name = "origen_nombre", nullable = false, length = 120)
	private String origenNombre;

	@Column(name = "origen_lat", nullable = false)
	private double origenLat;

	@Column(name = "origen_lon", nullable = false)
	private double origenLon;

	@Column(name = "destino_nombre", nullable = false, length = 120)
	private String destinoNombre;

	@Column(name = "destino_lat", nullable = false)
	private double destinoLat;

	@Column(name = "destino_lon", nullable = false)
	private double destinoLon;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoViaje estado = EstadoViaje.PROGRAMADO;

	@Column(name = "descripcion_carga", length = 255)
	private String descripcionCarga;

	@Column(name = "salida_programada")
	private Instant salidaProgramada;

	@Column(name = "salida_real")
	private Instant salidaReal;

	@Column(name = "llegada_estimada")
	private Instant llegadaEstimada;

	@Column(name = "llegada_real")
	private Instant llegadaReal;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void alCrear() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

}
