package um.edu.ar.omnitrucks.viaje.application.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * DTO inmutable record con el resumen de un envío para el listado del cliente.
 */
public record EnvioClienteResumenResponse(
	String codigoSeguimiento,
	EstadoViaje estado,
	String descripcionCarga,
	String origenNombre,
	String destinoNombre,
	Instant salidaProgramada,
	Instant llegadaEstimada
) {

	public static EnvioClienteResumenResponse desde(Viaje viaje) {
		return new EnvioClienteResumenResponse(
			viaje.getCodigoSeguimiento(),
			viaje.getEstado(),
			viaje.getDescripcionCarga(),
			viaje.getOrigenNombre(),
			viaje.getDestinoNombre(),
			viaje.getSalidaProgramada(),
			viaje.getLlegadaEstimada()
		);
	}

}
