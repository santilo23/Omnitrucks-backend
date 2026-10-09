package um.edu.ar.omnitrucks.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.auth.application.port.in.AutenticarUsuarioUseCase;
import um.edu.ar.omnitrucks.auth.domain.port.PasswordEncoderPort;
import um.edu.ar.omnitrucks.auth.domain.port.TokenServicePort;
import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;

/**
 * Implementación del caso de uso de autenticación.
 * Opera exclusivamente a través de los puertos de dominio, manteniendo la lógica
 * de negocio desacoplada de la infraestructura (Spring Security, JPA, JWT).
 */
@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

	private final UsuarioRepositoryPort usuarioRepository;
	private final PasswordEncoderPort passwordEncoder;
	private final TokenServicePort tokenService;

	public AutenticarUsuarioService(
			UsuarioRepositoryPort usuarioRepository,
			PasswordEncoderPort passwordEncoder,
			TokenServicePort tokenService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	@Override
	public LoginResponse ejecutar(LoginRequest command) {
		Usuario usuario = usuarioRepository.findByEmail(command.email().trim().toLowerCase())
				.orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas."));

		if (!usuario.isActivo()) {
			throw new CuentaInactivaException("La cuenta del usuario se encuentra inactiva.");
		}

		if (!passwordEncoder.matches(command.password(), usuario.getPasswordHash())) {
			throw new CredencialesInvalidasException("Credenciales inválidas.");
		}

		String token = tokenService.generarToken(usuario);
		return LoginResponse.de(token, usuario);
	}

}
