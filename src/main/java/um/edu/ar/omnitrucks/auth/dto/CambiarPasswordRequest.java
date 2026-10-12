package um.edu.ar.omnitrucks.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable record para la actualización de contraseña del usuario autenticado
 * (utilizado especialmente cuando inicia sesión con una clave provisoria asignada por el admin).
 */
public record CambiarPasswordRequest(
	@NotBlank(message = "La clave actual es requerida")
	String passwordActual,

	@NotBlank(message = "La nueva clave es requerida")
	@Size(min = 6, max = 100, message = "La nueva clave debe tener al menos 6 caracteres")
	String nuevaPassword
) {
}
