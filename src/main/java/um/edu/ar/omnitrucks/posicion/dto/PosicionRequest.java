package um.edu.ar.omnitrucks.posicion.dto;

import java.time.Instant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Una coordenada reportada por el dispositivo del camión.
 *
 * {@code registradoEn} es opcional: si el cliente no lo manda, se toma la hora
 * del servidor. Cuando el chofer estuvo sin señal y descarga un lote acumulado,
 * en cambio, cada posición trae el momento real en que fue tomada.
 */
public record PosicionRequest(

	@NotNull(message = "Falta la latitud.")
	@DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90.")
	@DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90.")
	Double latitud,

	@NotNull(message = "Falta la longitud.")
	@DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180.")
	@DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180.")
	Double longitud,

	@PositiveOrZero(message = "La velocidad no puede ser negativa.")
	Double velocidadKmh,

	@DecimalMin(value = "0.0", message = "El rumbo debe estar entre 0 y 360 grados.")
	@DecimalMax(value = "360.0", message = "El rumbo debe estar entre 0 y 360 grados.")
	Double rumboGrados,

	@PositiveOrZero(message = "La precisión no puede ser negativa.")
	Double precisionM,

	Instant registradoEn

) {
}
