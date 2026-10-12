package um.edu.ar.omnitrucks.viaje.application.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * Ubicación en vivo optimizada para el mapa del cliente asignado.
 */
public record UbicacionSeguimientoResponse(
	String codigoSeguimiento,
	EstadoViaje estado,
	double latitud,
	double longitud,
	Double velocidadKmh,
	Double rumboGrados,
	Instant registradoEn
) {

	public static UbicacionSeguimientoResponse desde(Viaje viaje, Posicion posicion) {
		if (posicion == null) {
			return new UbicacionSeguimientoResponse(
				viaje.getCodigoSeguimiento(),
				viaje.getEstado(),
				viaje.getOrigenLat(),
				viaje.getOrigenLon(),
				0.0,
				0.0,
				viaje.getCreatedAt()
			);
		}
		return new UbicacionSeguimientoResponse(
			viaje.getCodigoSeguimiento(),
			viaje.getEstado(),
			posicion.getLatitud(),
			posicion.getLongitud(),
			posicion.getVelocidadKmh(),
			posicion.getRumboGrados(),
			posicion.getRegistradoEn()
		);
	}

}
