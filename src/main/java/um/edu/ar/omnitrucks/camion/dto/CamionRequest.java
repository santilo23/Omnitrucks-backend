package um.edu.ar.omnitrucks.camion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que llegan para crear o editar un camión.
 *
 * La patente acepta los dos formatos argentinos vigentes: el viejo de tres
 * letras y tres números (ABC123) y el del Mercosur (AB123CD). Se normaliza a
 * mayúsculas en el servicio, así que la validación no distingue may/min.
 */
public record CamionRequest(

	@NotBlank(message = "La patente es obligatoria.")
	@Pattern(
		// Se toleran espacios alrededor y minúsculas: el servicio normaliza
		// la patente antes de guardarla.
		regexp = "(?i)^\\s*([A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2})\\s*$",
		message = "La patente debe tener formato ABC123 o AB123CD."
	)
	String patente,

	@NotBlank(message = "La marca es obligatoria.")
	@Size(max = 40, message = "La marca no puede superar los 40 caracteres.")
	String marca,

	@NotBlank(message = "El modelo es obligatorio.")
	@Size(max = 40, message = "El modelo no puede superar los 40 caracteres.")
	String modelo,

	@Min(value = 1950, message = "El año debe ser posterior a 1950.")
	@Max(value = 2100, message = "El año no parece válido.")
	Integer anio,

	@Positive(message = "La capacidad debe ser mayor a cero.")
	Integer capacidadKg

) {
}
