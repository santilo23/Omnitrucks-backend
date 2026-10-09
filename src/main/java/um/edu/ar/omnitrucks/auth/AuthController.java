package um.edu.ar.omnitrucks.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import um.edu.ar.omnitrucks.auth.application.port.in.AutenticarUsuarioUseCase;
import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

	public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
		this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest pedido) {
		LoginResponse respuesta = autenticarUsuarioUseCase.ejecutar(pedido);
		return ResponseEntity.ok(respuesta);
	}
}
