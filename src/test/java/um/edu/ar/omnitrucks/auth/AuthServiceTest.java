package um.edu.ar.omnitrucks.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	private AuthService authService;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);

		usuario = new Usuario();
		usuario.setId(10L);
		usuario.setEmail("chofer@omnitrucks.com");
		usuario.setPasswordHash("$2a$10$hashSeguroBCrypt");
		usuario.setNombre("Juan");
		usuario.setApellido("Pérez");
		usuario.setRol(Rol.CHOFER);
		usuario.setActivo(true);
	}

	@Test
	@DisplayName("Inicia sesión exitosamente y retorna LoginResponse con token")
	void loginExitoso() {
		when(usuarioRepository.findByEmail("chofer@omnitrucks.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("clave123", usuario.getPasswordHash())).thenReturn(true);
		when(jwtService.generarToken(usuario)).thenReturn("mocked.jwt.token");

		LoginResponse respuesta = authService.login(new LoginRequest("chofer@omnitrucks.com", "clave123"));

		assertThat(respuesta).isNotNull();
		assertThat(respuesta.token()).isEqualTo("mocked.jwt.token");
		assertThat(respuesta.usuarioId()).isEqualTo(10L);
		assertThat(respuesta.email()).isEqualTo("chofer@omnitrucks.com");
		assertThat(respuesta.rol()).isEqualTo(Rol.CHOFER);
		verify(jwtService).generarToken(usuario);
	}

	@Test
	@DisplayName("Falla si el email no existe en la base de datos")
	void fallaSiEmailNoExiste() {
		when(usuarioRepository.findByEmail("inexistente@omnitrucks.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(new LoginRequest("inexistente@omnitrucks.com", "clave123")))
			.isInstanceOf(CredencialesInvalidasException.class)
			.hasMessageContaining("Credenciales inválidas");
	}

	@Test
	@DisplayName("Falla si la contraseña no coincide con el hash")
	void fallaSiPasswordIncorrecto() {
		when(usuarioRepository.findByEmail("chofer@omnitrucks.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("claveErronea", usuario.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> authService.login(new LoginRequest("chofer@omnitrucks.com", "claveErronea")))
			.isInstanceOf(CredencialesInvalidasException.class)
			.hasMessageContaining("Credenciales inválidas");
	}

	@Test
	@DisplayName("Falla si la cuenta del usuario está inactiva o dada de baja")
	void fallaSiCuentaInactiva() {
		usuario.setActivo(false);
		when(usuarioRepository.findByEmail("chofer@omnitrucks.com")).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> authService.login(new LoginRequest("chofer@omnitrucks.com", "clave123")))
			.isInstanceOf(CuentaInactivaException.class)
			.hasMessageContaining("inactiva");
	}
}
