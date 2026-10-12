package um.edu.ar.omnitrucks.admin.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable record para el alta de un nuevo cliente por parte del administrador
 * con una contraseña provisoria asignada.
 */
public record CrearClienteRequest(
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

	@NotBlank(message = "La clave provisoria es obligatoria")
	@Size(min = 6, max = 100, message = "La clave provisoria debe tener al menos 6 caracteres")
	String passwordProvisoria
) {
}
