package um.edu.ar.omnitrucks.auth.application.port.in;

import um.edu.ar.omnitrucks.auth.dto.CambiarPasswordRequest;

/**
 * Caso de uso para la actualización de la contraseña del usuario autenticado.
 */
public interface CambiarPasswordUseCase {

	void ejecutar(String email, CambiarPasswordRequest request);

}
