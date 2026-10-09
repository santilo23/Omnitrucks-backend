package um.edu.ar.omnitrucks.auth.application.port.in;

import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;

/**
 * Caso de uso para la autenticación de usuarios.
 */
public interface AutenticarUsuarioUseCase {

	LoginResponse ejecutar(LoginRequest command);

}
