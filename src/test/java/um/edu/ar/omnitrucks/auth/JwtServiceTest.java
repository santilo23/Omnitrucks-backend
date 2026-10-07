package um.edu.ar.omnitrucks.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;

class JwtServiceTest {

	// Clave secreta fija de prueba (mínimo 256 bits = 32 bytes)
	private static final String CLAVE_SECRETA = "omnitrucks_secret_key_para_pruebas_unitarias_de_jwt_2026";
	private static final long EXPIRACION_MS = 86_400_000L; // 24 horas

	private JwtService jwtService;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(CLAVE_SECRETA, EXPIRACION_MS);

		usuario = new Usuario();
		usuario.setId(42L);
		usuario.setEmail("chofer@omnitrucks.com");
		usuario.setNombre("Carlos");
		usuario.setApellido("Camionero");
		usuario.setRol(Rol.CHOFER);
		usuario.setActivo(true);
	}

	@Test
	@DisplayName("Genera un token JWT válido y extrae el email del subject")
	void generaTokenYExtraeEmail() {
		String token = jwtService.generarToken(usuario);

		assertThat(token).isNotBlank();
		assertThat(jwtService.extraerEmail(token)).isEqualTo("chofer@omnitrucks.com");
	}

	@Test
	@DisplayName("Extrae el rol del claim correctamente")
	void extraeRolDelToken() {
		String token = jwtService.generarToken(usuario);

		Rol rolExtraido = jwtService.extraerRol(token);
		assertThat(rolExtraido).isEqualTo(Rol.CHOFER);
	}

	@Test
	@DisplayName("Valida exitosamente un token bien firmado y no vencido")
	void validaTokenCorrecto() {
		String token = jwtService.generarToken(usuario);

		boolean esValido = jwtService.esTokenValido(token, usuario.getEmail());
		assertThat(esValido).isTrue();
	}

	@Test
	@DisplayName("Rechaza un token si el email no coincide")
	void rechazaTokenConEmailDiferente() {
		String token = jwtService.generarToken(usuario);

		boolean esValido = jwtService.esTokenValido(token, "otro@omnitrucks.com");
		assertThat(esValido).isFalse();
	}

	@Test
	@DisplayName("Falla la validación si el token está expirado")
	void fallaSiElTokenEstaExpirado() {
		// Creamos un servicio con expiración negativa (-1000ms = ya vencido)
		JwtService jwtVencido = new JwtService(CLAVE_SECRETA, -1000L);
		String token = jwtVencido.generarToken(usuario);

		assertThatThrownBy(() -> jwtService.extraerEmail(token))
			.isInstanceOf(Exception.class);
	}
}
