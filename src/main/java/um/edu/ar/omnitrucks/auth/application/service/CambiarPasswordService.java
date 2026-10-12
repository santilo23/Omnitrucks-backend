package um.edu.ar.omnitrucks.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.auth.application.port.in.CambiarPasswordUseCase;
import um.edu.ar.omnitrucks.auth.domain.port.PasswordEncoderPort;
import um.edu.ar.omnitrucks.auth.dto.CambiarPasswordRequest;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;

@Service
@Transactional
public class CambiarPasswordService implements CambiarPasswordUseCase {

	private final UsuarioRepositoryPort usuarioRepository;
	private final PasswordEncoderPort passwordEncoder;

	public CambiarPasswordService(
			UsuarioRepositoryPort usuarioRepository,
			PasswordEncoderPort passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void ejecutar(String email, CambiarPasswordRequest request) {
		Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
				.orElseThrow(() -> new CredencialesInvalidasException("Usuario no encontrado."));

		if (!usuario.isActivo()) {
			throw new CuentaInactivaException("La cuenta del usuario se encuentra inactiva.");
		}

		if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
			throw new CredencialesInvalidasException("La contraseña actual es incorrecta.");
		}

		if (request.nuevaPassword().equals(request.passwordActual())) {
			throw new CredencialesInvalidasException("La nueva contraseña no puede ser idéntica a la anterior.");
		}

		usuario.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()));
		usuario.setDebeCambiarPassword(false);
		usuarioRepository.save(usuario);
	}

}
