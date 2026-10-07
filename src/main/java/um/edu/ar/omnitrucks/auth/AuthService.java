package um.edu.ar.omnitrucks.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;

/**
 * Servicio encargado de la lógica de autenticación y gestión de accesos.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	/**
	 * Autentica un usuario mediante sus credenciales (email y password)
	 * y emite su token JWT correspondiente.
	 */
	public LoginResponse login(LoginRequest pedido) {
		Usuario usuario = usuarioRepository.findByEmail(pedido.email().trim().toLowerCase())
				.orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas."));

		if (!usuario.isActivo()) {
			throw new CuentaInactivaException("La cuenta del usuario se encuentra inactiva.");
		}

		if (!passwordEncoder.matches(pedido.password(), usuario.getPasswordHash())) {
			throw new CredencialesInvalidasException("Credenciales inválidas.");
		}

		String token = jwtService.generarToken(usuario);
		return LoginResponse.de(token, usuario);
	}
}
