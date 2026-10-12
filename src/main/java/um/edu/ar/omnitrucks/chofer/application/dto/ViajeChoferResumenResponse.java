package um.edu.ar.omnitrucks.chofer.application.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * DTO inmutable record con el resumen de viaje asignado para el listado del chofer.
 */
public record ViajeChoferResumenResponse(
	Long viajeId,
	String codigoSeguimiento,
	EstadoViaje estado,
	String patenteCamion,
	String modeloCamion,
	String origenNombre,
	String destinoNombre,
	Instant salidaProgramada,
	Instant llegadaEstimada
) {

	public static ViajeChoferResumenResponse desde(Viaje viaje) {
		return new ViajeChoferResumenResponse(
			viaje.getId(),
			viaje.getCodigoSeguimiento(),
			viaje.getEstado(),
			viaje.getCamion().getPatente(),
			viaje.getCamion().getMarca() + " " + viaje.getCamion().getModelo(),
			viaje.getOrigenNombre(),
			viaje.getDestinoNombre(),
			viaje.getSalidaProgramada(),
			viaje.getLlegadaEstimada()
		);
	}

}
