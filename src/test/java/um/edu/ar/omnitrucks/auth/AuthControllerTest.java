package um.edu.ar.omnitrucks.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private um.edu.ar.omnitrucks.viaje.ViajeRepository viajes;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void prepararUsuario() {
		viajes.deleteAllInBatch();
		usuarioRepository.deleteAllInBatch();

		Usuario chofer = new Usuario();
		chofer.setEmail("chofer.test@omnitrucks.com");
		chofer.setPasswordHash(passwordEncoder.encode("secreto123"));
		chofer.setNombre("Mario");
		chofer.setApellido("Baracus");
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);
		usuarioRepository.save(chofer);
	}

	@Test
	@DisplayName("POST /api/auth/login exitoso devuelve 200 OK y token JWT")
	void loginExitoso() throws Exception {
		String body = """
			{
				"email": "chofer.test@omnitrucks.com",
				"password": "secreto123"
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.tipo").value("Bearer"))
			.andExpect(jsonPath("$.email").value("chofer.test@omnitrucks.com"))
			.andExpect(jsonPath("$.nombreCompleto").value("Mario Baracus"))
			.andExpect(jsonPath("$.rol").value("CHOFER"));
	}

	@Test
	@DisplayName("POST /api/auth/login con clave incorrecta devuelve 401 Unauthorized")
	void loginClaveErronea() throws Exception {
		String body = """
			{
				"email": "chofer.test@omnitrucks.com",
				"password": "clave_erronea"
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.title").value("Credenciales inválidas"));
	}

	@Test
	@DisplayName("POST /api/auth/login con email inexistente devuelve 401 Unauthorized")
	void loginEmailInexistente() throws Exception {
		String body = """
			{
				"email": "nadie@omnitrucks.com",
				"password": "secreto123"
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.title").value("Credenciales inválidas"));
	}

	@Test
	@DisplayName("POST /api/auth/login con campos vacíos devuelve 400 Bad Request")
	void loginCamposInvalidos() throws Exception {
		String body = """
			{
				"email": "formato-invalido",
				"password": ""
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Datos inválidos"));
	}

	@Test
	@DisplayName("POST /api/auth/login con cuenta inactiva devuelve 403 Forbidden")
	void loginCuentaInactiva() throws Exception {
		Usuario inactivo = new Usuario();
		inactivo.setEmail("inactivo@omnitrucks.com");
		inactivo.setPasswordHash(passwordEncoder.encode("secreto123"));
		inactivo.setNombre("Pedro");
		inactivo.setApellido("Picapiedra");
		inactivo.setRol(Rol.CHOFER);
		inactivo.setActivo(false);
		usuarioRepository.save(inactivo);

		String body = """
			{
				"email": "inactivo@omnitrucks.com",
				"password": "secreto123"
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.title").value("Cuenta inactiva"));
	}

	@Test
	@DisplayName("POST /api/auth/login para rol CLIENTE devuelve DTO record con rol CLIENTE")
	void loginClienteExitoso() throws Exception {
		Usuario cliente = new Usuario();
		cliente.setEmail("empresa@cliente.com");
		cliente.setPasswordHash(passwordEncoder.encode("secreto123"));
		cliente.setNombre("Distribuidora");
		cliente.setApellido("Sur");
		cliente.setRol(Rol.CLIENTE);
		cliente.setActivo(true);
		usuarioRepository.save(cliente);

		String body = """
			{
				"email": "empresa@cliente.com",
				"password": "secreto123"
			}
			""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.rol").value("CLIENTE"))
			.andExpect(jsonPath("$.token").isString());
	}

	@Test
	@DisplayName("GET /api/viajes/activos sin autenticación es rechazado (Login obligatorio para mapa)")
	void accesoAlMapaSinAutenticacionEsRechazado() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/viajes/activos"))
			.andExpect(status().isForbidden());
	}
}
