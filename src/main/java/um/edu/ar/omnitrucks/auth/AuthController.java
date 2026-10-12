package um.edu.ar.omnitrucks.auth;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import um.edu.ar.omnitrucks.auth.application.port.in.AutenticarUsuarioUseCase;
import um.edu.ar.omnitrucks.auth.application.port.in.CambiarPasswordUseCase;
import um.edu.ar.omnitrucks.auth.dto.CambiarPasswordRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
	private final CambiarPasswordUseCase cambiarPasswordUseCase;

	public AuthController(
			AutenticarUsuarioUseCase autenticarUsuarioUseCase,
			CambiarPasswordUseCase cambiarPasswordUseCase) {
		this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
		this.cambiarPasswordUseCase = cambiarPasswordUseCase;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest pedido) {
		LoginResponse respuesta = autenticarUsuarioUseCase.ejecutar(pedido);
		return ResponseEntity.ok(respuesta);
	}

	@PostMapping("/cambiar-password")
	public ResponseEntity<Void> cambiarPassword(
			@Valid @RequestBody CambiarPasswordRequest pedido,
			Authentication authentication) {
		cambiarPasswordUseCase.ejecutar(authentication.getName(), pedido);
		return ResponseEntity.ok().build();
	}
}
