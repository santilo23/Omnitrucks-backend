package um.edu.ar.omnitrucks.viaje.dto;

import java.time.Instant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ViajeRequest(

	@NotNull(message = "Hay que indicar el camión.")
	Long camionId,

	@NotNull(message = "Hay que indicar el chofer.")
	Long choferId,

	/** Opcional: el cliente que sigue el envío. */
	Long clienteId,

	@NotBlank(message = "El origen es obligatorio.")
	@Size(max = 120, message = "El nombre del origen es demasiado largo.")
	String origenNombre,

	@NotNull(message = "Falta la latitud de origen.")
	@DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90.")
	@DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90.")
	Double origenLat,

	@NotNull(message = "Falta la longitud de origen.")
	@DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180.")
	@DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180.")
	Double origenLon,

	@NotBlank(message = "El destino es obligatorio.")
	@Size(max = 120, message = "El nombre del destino es demasiado largo.")
	String destinoNombre,

	@NotNull(message = "Falta la latitud de destino.")
	@DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90.")
	@DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90.")
	Double destinoLat,

	@NotNull(message = "Falta la longitud de destino.")
	@DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180.")
	@DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180.")
	Double destinoLon,

	@Size(max = 255, message = "La descripción de la carga es demasiado larga.")
	String descripcionCarga,

	Instant salidaProgramada

) {
}
