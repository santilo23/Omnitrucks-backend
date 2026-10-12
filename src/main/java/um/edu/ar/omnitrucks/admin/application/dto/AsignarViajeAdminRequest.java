package um.edu.ar.omnitrucks.admin.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable record para la asignación de viajes a choferes desde el panel de administración.
 */
public record AsignarViajeAdminRequest(
	@NotNull(message = "El camión es obligatorio")
	Long camionId,

	@NotNull(message = "El chofer asignado es obligatorio")
	Long choferId,

	Long clienteId,

	@NotBlank(message = "El nombre del origen es obligatorio")
	@Size(max = 120, message = "El nombre de origen es demasiado largo")
	String origenNombre,

	@NotNull(message = "La latitud de origen es obligatoria")
	@DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
	Double origenLat,

	@NotNull(message = "La longitud de origen es obligatoria")
	@DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
	Double origenLon,

	@NotBlank(message = "El nombre del destino es obligatorio")
	@Size(max = 120, message = "El nombre de destino es demasiado largo")
	String destinoNombre,

	@NotNull(message = "La latitud de destino es obligatoria")
	@DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
	Double destinoLat,

	@NotNull(message = "La longitud de destino es obligatoria")
	@DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
	Double destinoLon,

	@Size(max = 255, message = "La descripción de carga no puede exceder 255 caracteres")
	String descripcionCarga,

	@Size(max = 32, message = "El código de seguimiento no puede exceder 32 caracteres")
	String codigoSeguimiento,

	boolean iniciarInmediatamente
) {
}
