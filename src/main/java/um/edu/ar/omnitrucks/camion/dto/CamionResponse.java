package um.edu.ar.omnitrucks.camion.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.camion.Camion;

/**
 * Representación de un camión hacia afuera de la API.
 *
 * Se devuelve este record y nunca la entidad: exponer entidades JPA filtra
 * campos que no corresponden y dispara consultas lazy al serializar.
 */
public record CamionResponse(
	Long id,
	String patente,
	String marca,
	String modelo,
	Integer anio,
	Integer capacidadKg,
	boolean activo,
	Instant createdAt
) {

	public static CamionResponse desde(Camion camion) {
		return new CamionResponse(
			camion.getId(),
			camion.getPatente(),
			camion.getMarca(),
			camion.getModelo(),
			camion.getAnio(),
			camion.getCapacidadKg(),
			camion.isActivo(),
			camion.getCreatedAt()
		);
	}

}
