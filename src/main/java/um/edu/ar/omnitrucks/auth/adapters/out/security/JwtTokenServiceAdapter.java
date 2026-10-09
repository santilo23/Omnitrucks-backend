package um.edu.ar.omnitrucks.auth.adapters.out.security;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.auth.JwtService;
import um.edu.ar.omnitrucks.auth.domain.port.TokenServicePort;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

/**
 * Adaptador de salida que conecta el puerto TokenServicePort con el
 * motor criptográfico JwtService.
 */
@Component
public class JwtTokenServiceAdapter implements TokenServicePort {

	private final JwtService jwtService;

	public JwtTokenServiceAdapter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public String generarToken(Usuario usuario) {
		return jwtService.generarToken(usuario);
	}

	@Override
	public String extraerEmail(String token) {
		return jwtService.extraerEmail(token);
	}

	@Override
	public Rol extraerRol(String token) {
		return jwtService.extraerRol(token);
	}

	@Override
	public boolean esTokenValido(String token, String email) {
		return jwtService.esTokenValido(token, email);
	}

}
