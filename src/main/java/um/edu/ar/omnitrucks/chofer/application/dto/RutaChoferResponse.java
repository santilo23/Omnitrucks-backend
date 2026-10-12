package um.edu.ar.omnitrucks.chofer.application.dto;

import java.time.Instant;
import java.util.List;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * DTO inmutable record con la información integral de la ruta asignada al chofer y su destino final.
 * REGLA ESTRICTA DE PRIVACIDAD: Provee únicamente los datos del camión y la carga asociados a este chofer,
 * sin exponer jamás vehículos o rutas pertenecientes a otros conductores.
 */
public record RutaChoferResponse(
	Long viajeId,
	String codigoSeguimiento,
	EstadoViaje estado,
	VehiculoChoferResponse vehiculo,
	PuntoRuta origen,
	PuntoRuta destino,
	String descripcionCarga,
	Instant salidaProgramada,
	Instant salidaReal,
	Instant llegadaEstimada,
	PosicionChoferResponse ultimaUbicacion,
	List<CoordenadaChoferResponse> recorrido
) {

	public record VehiculoChoferResponse(
		Long id,
		String patente,
		String marca,
		String modelo,
		Integer anio,
		Integer capacidadKg
	) {
	}

	public record PuntoRuta(
		String nombre,
		double latitud,
		double longitud
	) {
	}

	public record PosicionChoferResponse(
		double latitud,
		double longitud,
		Double velocidadKmh,
		Double rumboGrados,
		Instant registradoEn
	) {
	}

	public record CoordenadaChoferResponse(
		double latitud,
		double longitud,
		Instant registradoEn
	) {
	}

	public static RutaChoferResponse desde(Viaje viaje, Posicion ultimaPosicion, List<Posicion> posicionesRecorrido) {
		VehiculoChoferResponse vehiculo = new VehiculoChoferResponse(
			viaje.getCamion().getId(),
			viaje.getCamion().getPatente(),
			viaje.getCamion().getMarca(),
			viaje.getCamion().getModelo(),
			viaje.getCamion().getAnio(),
			viaje.getCamion().getCapacidadKg()
		);

		PuntoRuta origen = new PuntoRuta(
			viaje.getOrigenNombre(),
			viaje.getOrigenLat(),
			viaje.getOrigenLon()
		);

		PuntoRuta destino = new PuntoRuta(
			viaje.getDestinoNombre(),
			viaje.getDestinoLat(),
			viaje.getDestinoLon()
		);

		PosicionChoferResponse ultima = (ultimaPosicion != null)
			? new PosicionChoferResponse(
				ultimaPosicion.getLatitud(),
				ultimaPosicion.getLongitud(),
				ultimaPosicion.getVelocidadKmh(),
				ultimaPosicion.getRumboGrados(),
				ultimaPosicion.getRegistradoEn()
			)
			: null;

		List<CoordenadaChoferResponse> recorrido = (posicionesRecorrido != null)
			? posicionesRecorrido.stream()
				.map(p -> new CoordenadaChoferResponse(p.getLatitud(), p.getLongitud(), p.getRegistradoEn()))
				.toList()
			: List.of();

		return new RutaChoferResponse(
			viaje.getId(),
			viaje.getCodigoSeguimiento(),
			viaje.getEstado(),
			vehiculo,
			origen,
			destino,
			viaje.getDescripcionCarga(),
			viaje.getSalidaProgramada(),
			viaje.getSalidaReal(),
			viaje.getLlegadaEstimada(),
			ultima,
			recorrido
		);
	}

}
