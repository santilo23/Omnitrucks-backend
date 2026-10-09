package um.edu.ar.omnitrucks.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import um.edu.ar.omnitrucks.auth.application.service.AutenticarUsuarioService;
import um.edu.ar.omnitrucks.auth.domain.port.PasswordEncoderPort;
import um.edu.ar.omnitrucks.auth.domain.port.TokenServicePort;
import um.edu.ar.omnitrucks.auth.dto.LoginRequest;
import um.edu.ar.omnitrucks.auth.dto.LoginResponse;
import um.edu.ar.omnitrucks.common.error.CredencialesInvalidasException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioServiceTest {

	@Mock
	private UsuarioRepositoryPort usuarioRepository;

	@Mock
	private PasswordEncoderPort passwordEncoder;

	@Mock
	private TokenServicePort tokenService;

	private AutenticarUsuarioService useCase;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		useCase = new AutenticarUsuarioService(usuarioRepository, passwordEncoder, tokenService);

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
	@DisplayName("Inicia sesión exitosamente y retorna LoginResponse con token a través de los puertos")
	void loginExitoso() {
		when(usuarioRepository.findByEmail("chofer@omnitrucks.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("clave123", usuario.getPasswordHash())).thenReturn(true);
		when(tokenService.generarToken(usuario)).thenReturn("mocked.jwt.token");

		LoginResponse respuesta = useCase.ejecutar(new LoginRequest("chofer@omnitrucks.com", "clave123"));

		assertThat(respuesta).isNotNull();
		assertThat(respuesta.token()).isEqualTo("mocked.jwt.token");
		assertThat(respuesta.usuarioId()).isEqualTo(10L);
		assertThat(respuesta.email()).isEqualTo("chofer@omnitrucks.com");
		assertThat(respuesta.rol()).isEqualTo(Rol.CHOFER);
		verify(tokenService).generarToken(usuario);
	}

	@Test
	@DisplayName("Falla si el email no existe en el repositorio de usuarios")
	void fallaSiEmailNoExiste() {
		when(usuarioRepository.findByEmail("inexistente@omnitrucks.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.ejecutar(new LoginRequest("inexistente@omnitrucks.com", "clave123")))
			.isInstanceOf(CredencialesInvalidasException.class)
			.hasMessageContaining("Credenciales inválidas");
	}

	@Test
	@DisplayName("Falla si la contraseña no coincide mediante el PasswordEncoderPort")
	void fallaSiPasswordIncorrecto() {
		when(usuarioRepository.findByEmail("chofer@omnitrucks.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("claveErronea", usuario.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> useCase.ejecutar(new LoginRequest("chofer@omnitrucks.com", "claveErronea")))
			.isInstanceOf(CredencialesInvalidasException.class)
			.hasMessageContaining("Credenciales inválidas");
	}

	@Test
	@DisplayName("Falla si la cuenta del usuario está inactiva")
	void fallaSiCuentaInactiva() {
		usuario.setActivo(false);
		when(usuarioRepository.findByEmail("chofer@omnitrucks.com")).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> useCase.ejecutar(new LoginRequest("chofer@omnitrucks.com", "clave123")))
			.isInstanceOf(CuentaInactivaException.class)
			.hasMessageContaining("inactiva");
	}
}
