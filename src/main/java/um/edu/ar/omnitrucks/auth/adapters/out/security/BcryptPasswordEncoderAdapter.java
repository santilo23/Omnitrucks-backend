package um.edu.ar.omnitrucks.auth.adapters.out.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.auth.domain.port.PasswordEncoderPort;

/**
 * Adaptador de salida que conecta el puerto PasswordEncoderPort con la
 * implementación BCrypt de Spring Security.
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

	private final PasswordEncoder passwordEncoder;

	public BcryptPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

}
