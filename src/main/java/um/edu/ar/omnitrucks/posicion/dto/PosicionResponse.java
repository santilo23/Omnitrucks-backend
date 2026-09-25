package um.edu.ar.omnitrucks.posicion.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.posicion.Posicion;

public record PosicionResponse(
	Long id,
	double latitud,
	double longitud,
	Double velocidadKmh,
	Double rumboGrados,
	Double precisionM,
	Instant registradoEn,
	Instant recibidoEn
) {

	public static PosicionResponse desde(Posicion posicion) {
		return new PosicionResponse(
			posicion.getId(),
			posicion.getLatitud(),
			posicion.getLongitud(),
			posicion.getVelocidadKmh(),
			posicion.getRumboGrados(),
			posicion.getPrecisionM(),
			posicion.getRegistradoEn(),
			posicion.getRecibidoEn()
		);
	}

}
