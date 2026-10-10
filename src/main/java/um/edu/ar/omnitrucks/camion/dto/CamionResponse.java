package um.edu.ar.omnitrucks.camion.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.domain.model.EstadoOperativoCamion;

/**
 * Representación de un camión hacia afuera de la API con su estado operativo.
 */
public record CamionResponse(
	Long id,
	String patente,
	String marca,
	String modelo,
	Integer anio,
	Integer capacidadKg,
	boolean activo,
	EstadoOperativoCamion estadoOperativo,
	Instant createdAt
) {

	public static CamionResponse desde(Camion camion, EstadoOperativoCamion estadoOperativo) {
		return new CamionResponse(
			camion.getId(),
			camion.getPatente(),
			camion.getMarca(),
			camion.getModelo(),
			camion.getAnio(),
			camion.getCapacidadKg(),
			camion.isActivo(),
			estadoOperativo,
			camion.getCreatedAt()
		);
	}

	public static CamionResponse desde(Camion camion) {
		return desde(
			camion,
			camion.isActivo() ? EstadoOperativoCamion.DISPONIBLE : EstadoOperativoCamion.INACTIVO
		);
	}

}
