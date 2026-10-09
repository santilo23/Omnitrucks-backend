package um.edu.ar.omnitrucks.usuario.dto;

import java.time.Instant;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

/**
 * DTO inmutable (record) que expone los datos de un usuario sin revelar información sensible.
 */
public record UsuarioResponse(
	Long id,
	String email,
	String nombre,
	String apellido,
	String nombreCompleto,
	Rol rol,
	boolean activo,
	Instant createdAt
) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(
			usuario.getId(),
			usuario.getEmail(),
			usuario.getNombre(),
			usuario.getApellido(),
			usuario.getNombreCompleto(),
			usuario.getRol(),
			usuario.isActivo(),
			usuario.getCreatedAt()
		);
	}

}
