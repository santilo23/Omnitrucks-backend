package um.edu.ar.omnitrucks.viaje.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

public record ViajeResponse(
	Long id,
	EstadoViaje estado,
	CamionResumen camion,
	PersonaResumen chofer,
	PersonaResumen cliente,
	Punto origen,
	Punto destino,
	String descripcionCarga,
	Instant salidaProgramada,
	Instant salidaReal,
	Instant llegadaEstimada,
	Instant llegadaReal,
	Instant createdAt
) {

	public record CamionResumen(Long id, String patente, String marca, String modelo) {

		static CamionResumen desde(Camion camion) {
			return new CamionResumen(camion.getId(), camion.getPatente(), camion.getMarca(), camion.getModelo());
		}
	}

	public record PersonaResumen(Long id, String nombreCompleto, String email) {

		static PersonaResumen desde(Usuario usuario) {
			return usuario == null
				? null
				: new PersonaResumen(usuario.getId(), usuario.getNombreCompleto(), usuario.getEmail());
		}
	}

	public record Punto(String nombre, double latitud, double longitud) {
	}

	public static ViajeResponse desde(Viaje viaje) {
		return new ViajeResponse(
			viaje.getId(),
			viaje.getEstado(),
			CamionResumen.desde(viaje.getCamion()),
			PersonaResumen.desde(viaje.getChofer()),
			PersonaResumen.desde(viaje.getCliente()),
			new Punto(viaje.getOrigenNombre(), viaje.getOrigenLat(), viaje.getOrigenLon()),
			new Punto(viaje.getDestinoNombre(), viaje.getDestinoLat(), viaje.getDestinoLon()),
			viaje.getDescripcionCarga(),
			viaje.getSalidaProgramada(),
			viaje.getSalidaReal(),
			viaje.getLlegadaEstimada(),
			viaje.getLlegadaReal(),
			viaje.getCreatedAt()
		);
	}

}
