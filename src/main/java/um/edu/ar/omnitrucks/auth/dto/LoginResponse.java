package um.edu.ar.omnitrucks.auth.dto;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

public record LoginResponse(
	String token,
	String tipo,
	Long usuarioId,
	String email,
	String nombreCompleto,
	Rol rol,
	boolean debeCambiarPassword
) {
	public static LoginResponse de(String token, Usuario usuario) {
		return new LoginResponse(
			token,
			"Bearer",
			usuario.getId(),
			usuario.getEmail(),
			usuario.getNombreCompleto(),
			usuario.getRol(),
			usuario.isDebeCambiarPassword()
		);
	}
}
