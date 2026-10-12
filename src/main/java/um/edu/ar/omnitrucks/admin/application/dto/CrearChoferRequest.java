package um.edu.ar.omnitrucks.admin.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable record para la creación y alta de un nuevo chofer por parte del administrador.
 * Regla de seguridad: La clave en texto plano recibida solo existe en memoria durante la petición
 * y se hashea inmediatamente con BCrypt antes de persistirse en base de datos.
 */
public record CrearChoferRequest(
	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
	String nombre,

	@NotBlank(message = "El apellido es obligatorio")
	@Size(max = 80, message = "El apellido no puede exceder 80 caracteres")
	String apellido,

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El formato de email no es válido")
	@Size(max = 120, message = "El email no puede exceder 120 caracteres")
	String email,

	@NotBlank(message = "La clave de acceso es obligatoria")
	@Size(min = 6, max = 100, message = "La clave debe tener entre 6 y 100 caracteres")
	String password
) {
}
