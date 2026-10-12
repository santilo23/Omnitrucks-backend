package um.edu.ar.omnitrucks.viaje.application.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * DTO inmutable record para la consulta pública de un cliente.
 * REGLA ESTRICTA DE PRIVACIDAD: Oculta identificadores internos de base de datos,
 * datos personales sensibles del chofer (email, teléfono, id) y datos de flota
 * sensibles del camión (id, año de compra, capacidad interna, etc.).
 */
public record SeguimientoClienteResponse(
	String codigoSeguimiento,
	EstadoViaje estado,
	String descripcionCarga,
	PuntoPublico origen,
	PuntoPublico destino,
	VehiculoPublico vehiculo,
	ChoferPublico chofer,
	UbicacionPublica ubicacionActual,
	Instant salidaProgramada,
	Instant salidaReal,
	Instant llegadaEstimada,
	Instant llegadaReal
) {

	public record PuntoPublico(String nombre, double latitud, double longitud) {
	}

	public record VehiculoPublico(String patente, String marca, String modelo) {
	}

	public record ChoferPublico(String nombre) {
	}

	public record UbicacionPublica(double latitud, double longitud, Instant registradoEn) {
	}

	public static SeguimientoClienteResponse desde(Viaje viaje, Posicion ultimaPosicion) {
		PuntoPublico origen = new PuntoPublico(
			viaje.getOrigenNombre(),
			viaje.getOrigenLat(),
			viaje.getOrigenLon()
		);

		PuntoPublico destino = new PuntoPublico(
			viaje.getDestinoNombre(),
			viaje.getDestinoLat(),
			viaje.getDestinoLon()
		);

		VehiculoPublico vehiculo = new VehiculoPublico(
			viaje.getCamion().getPatente(),
			viaje.getCamion().getMarca(),
			viaje.getCamion().getModelo()
		);

		ChoferPublico chofer = new ChoferPublico(
			viaje.getChofer() != null ? viaje.getChofer().getNombreCompleto() : "Chofer Asignado"
		);

		UbicacionPublica ubicacion = (ultimaPosicion != null)
			? new UbicacionPublica(
				ultimaPosicion.getLatitud(),
				ultimaPosicion.getLongitud(),
				ultimaPosicion.getRegistradoEn()
			)
			: null;

		return new SeguimientoClienteResponse(
			viaje.getCodigoSeguimiento(),
			viaje.getEstado(),
			viaje.getDescripcionCarga(),
			origen,
			destino,
			vehiculo,
			chofer,
			ubicacion,
			viaje.getSalidaProgramada(),
			viaje.getSalidaReal(),
			viaje.getLlegadaEstimada(),
			viaje.getLlegadaReal()
		);
	}

}
