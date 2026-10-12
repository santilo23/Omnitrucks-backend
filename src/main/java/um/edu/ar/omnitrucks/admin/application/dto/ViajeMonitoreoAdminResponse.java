package um.edu.ar.omnitrucks.admin.application.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

/**
 * DTO inmutable record para el monitoreo de choferes y viajes en el panel del administrador.
 * Incluye de manera destacada el código de seguimiento asignado para ser entregado al cliente.
 */
public record ViajeMonitoreoAdminResponse(
	Long viajeId,
	String codigoSeguimiento,
	EstadoViaje estado,
	Long choferId,
	String choferNombre,
	String choferEmail,
	Long camionId,
	String camionPatente,
	String camionModelo,
	String origenNombre,
	String destinoNombre,
	String descripcionCarga,
	Instant salidaProgramada,
	Instant salidaReal,
	Double latitudActual,
	Double longitudActual,
	Double velocidadKmh,
	Double rumboGrados,
	Instant ultimaActualizacion
) {

	public static ViajeMonitoreoAdminResponse desde(Viaje viaje, Posicion ultimaPosicion) {
		String choferNombre = (viaje.getChofer() != null) ? viaje.getChofer().getNombreCompleto() : "Sin chofer";
		String choferEmail = (viaje.getChofer() != null) ? viaje.getChofer().getEmail() : "";
		Long choferId = (viaje.getChofer() != null) ? viaje.getChofer().getId() : null;

		Long camionId = (viaje.getCamion() != null) ? viaje.getCamion().getId() : null;
		String camionPatente = (viaje.getCamion() != null) ? viaje.getCamion().getPatente() : "";
		String camionModelo = (viaje.getCamion() != null)
			? viaje.getCamion().getMarca() + " " + viaje.getCamion().getModelo()
			: "";

		Double lat = (ultimaPosicion != null) ? ultimaPosicion.getLatitud() : viaje.getOrigenLat();
		Double lon = (ultimaPosicion != null) ? ultimaPosicion.getLongitud() : viaje.getOrigenLon();
		Double vel = (ultimaPosicion != null) ? ultimaPosicion.getVelocidadKmh() : 0.0;
		Double rumbo = (ultimaPosicion != null) ? ultimaPosicion.getRumboGrados() : 0.0;
		Instant act = (ultimaPosicion != null) ? ultimaPosicion.getRegistradoEn() : viaje.getCreatedAt();

		return new ViajeMonitoreoAdminResponse(
			viaje.getId(),
			viaje.getCodigoSeguimiento(),
			viaje.getEstado(),
			choferId,
			choferNombre,
			choferEmail,
			camionId,
			camionPatente,
			camionModelo,
			viaje.getOrigenNombre(),
			viaje.getDestinoNombre(),
			viaje.getDescripcionCarga(),
			viaje.getSalidaProgramada(),
			viaje.getSalidaReal(),
			lat,
			lon,
			vel,
			rumbo,
			act
		);
	}

}
