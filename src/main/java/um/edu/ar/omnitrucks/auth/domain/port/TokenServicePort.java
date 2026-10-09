package um.edu.ar.omnitrucks.auth.domain.port;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

/**
 * Puerto de dominio que abstrae la emisión y validación de tokens
 * de autenticación (ej. JWT, Paseto).
 */
public interface TokenServicePort {

	String generarToken(Usuario usuario);

	String extraerEmail(String token);

	Rol extraerRol(String token);

	boolean esTokenValido(String token, String email);

}
