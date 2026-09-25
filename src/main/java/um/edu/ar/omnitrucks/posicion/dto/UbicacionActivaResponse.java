package um.edu.ar.omnitrucks.posicion.dto;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * Dónde está ahora un camión que está en ruta.
 *
 * Trae lo mínimo que el mapa necesita para dibujar un marcador y etiquetarlo,
 * sin arrastrar el viaje completo.
 */
public record UbicacionActivaResponse(
	Long viajeId,
	String patente,
	EstadoViaje estado,
	String destinoNombre,
	PosicionResponse posicion
) {

	public static UbicacionActivaResponse desde(Posicion posicion) {
		Viaje viaje = posicion.getViaje();

		return new UbicacionActivaResponse(
			viaje.getId(),
			viaje.getCamion().getPatente(),
			viaje.getEstado(),
			viaje.getDestinoNombre(),
			PosicionResponse.desde(posicion)
		);
	}

}
