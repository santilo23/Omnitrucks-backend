package um.edu.ar.omnitrucks.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest pedido) {
		LoginResponse respuesta = authService.login(pedido);
		return ResponseEntity.ok(respuesta);
	}
}
