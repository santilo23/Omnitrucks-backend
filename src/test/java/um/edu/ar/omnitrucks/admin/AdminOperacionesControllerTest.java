package um.edu.ar.omnitrucks.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

@SpringBootTest
@Transactional
class AdminOperacionesControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private CamionRepository camionRepository;

	@Autowired
	private ViajeRepository viajeRepository;

	@Autowired
	private PosicionRepository posicionRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Camion camion;
	private Usuario chofer;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		posicionRepository.deleteAllInBatch();
		viajeRepository.deleteAllInBatch();
		camionRepository.deleteAll();
		usuarioRepository.deleteAllInBatch();

		camion = new Camion();
		camion.setPatente("AD999ZZ");
		camion.setMarca("Scania");
		camion.setModelo("R450");
		camion.setAnio(2023);
		camion.setCapacidadKg(32000);
		camion.setActivo(true);
		camion = camionRepository.save(camion);

		chofer = new Usuario();
		chofer.setNombre("Roberto");
		chofer.setApellido("Galán");
		chofer.setEmail("roberto.galan@omnitrucks.com");
		chofer.setPasswordHash(passwordEncoder.encode("clave123"));
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);
		chofer = usuarioRepository.save(chofer);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede agregar un nuevo chofer con clave segura hasheada con BCrypt")
	void adminPuedeCrearChofer() throws Exception {
		String json = """
			{
				"nombre": "Carlos",
				"apellido": "Monzón",
				"email": "carlos.monzon@omnitrucks.com",
				"password": "miClaveSegura123"
			}
			""";

		mockMvc.perform(post("/api/admin/choferes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.nombre", is("Carlos")))
			.andExpect(jsonPath("$.apellido", is("Monzón")))
			.andExpect(jsonPath("$.email", is("carlos.monzon@omnitrucks.com")))
			.andExpect(jsonPath("$.rol", is("CHOFER")))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist());

		// Valida en base de datos que se haya guardado con BCrypt (no texto plano)
		Usuario guardado = usuarioRepository.findByEmail("carlos.monzon@omnitrucks.com").orElseThrow();
		assertThat(guardado.getPasswordHash()).startsWith("$2a$");
		assertThat(passwordEncoder.matches("miClaveSegura123", guardado.getPasswordHash())).isTrue();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede listar los choferes registrados en el sistema")
	void adminPuedeListarChoferes() throws Exception {
		mockMvc.perform(get("/api/admin/choferes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].email", is("roberto.galan@omnitrucks.com")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede asignar viaje a un chofer e iniciar la simulación en ruta inmediatamente")
	void adminPuedeAsignarViajeEIniciarSimulacion() throws Exception {
		String json = """
			{
				"camionId": %d,
				"choferId": %d,
				"origenNombre": "Buenos Aires (Terminal Retiro)",
				"origenLat": -34.5888,
				"origenLon": -58.3750,
				"destinoNombre": "Rosario (Puerto)",
				"destinoLat": -32.9468,
				"destinoLon": -60.6393,
				"descripcionCarga": "Carga de alimentos no perecederos",
				"iniciarInmediatamente": true
			}
			""".formatted(camion.getId(), chofer.getId());

		mockMvc.perform(post("/api/admin/viajes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.codigoSeguimiento", notNullValue()))
			.andExpect(jsonPath("$.estado", is("EN_CURSO")))
			.andExpect(jsonPath("$.choferNombre", is("Roberto Galán")))
			.andExpect(jsonPath("$.camionPatente", is("AD999ZZ")))
			.andExpect(jsonPath("$.latitudActual", is(-34.5888)))
			.andExpect(jsonPath("$.longitudActual", is(-58.3750)));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede listar choferes en conducción mostrando el código de seguimiento para el cliente")
	void adminPuedeVerChoferesEnConduccionConCodigoParaCliente() throws Exception {
		// Asignamos e iniciamos un viaje primero
		adminPuedeAsignarViajeEIniciarSimulacion();

		mockMvc.perform(get("/api/admin/viajes/activos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].codigoSeguimiento", notNullValue()))
			.andExpect(jsonPath("$[0].choferNombre", is("Roberto Galán")))
			.andExpect(jsonPath("$[0].camionPatente", is("AD999ZZ")))
			.andExpect(jsonPath("$[0].estado", is("EN_CURSO")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede generar un viaje de prueba simulado con un solo click")
	void adminPuedeGenerarViajeSimuladoDemo() throws Exception {
		mockMvc.perform(post("/api/admin/simulacion/crear-demo"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.codigoSeguimiento", notNullValue()))
			.andExpect(jsonPath("$.estado", is("EN_CURSO")))
			.andExpect(jsonPath("$.latitudActual", notNullValue()))
			.andExpect(jsonPath("$.longitudActual", notNullValue()));
	}

	@Test
	@WithMockUser(roles = "CHOFER")
	@DisplayName("Un chofer NO tiene permiso para crear otros choferes ni asignar viajes de administración (403)")
	void choferNoPuedeAccederOperacionesAdmin() throws Exception {
		mockMvc.perform(post("/api/admin/choferes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/choferes"))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/admin/viajes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isForbidden());
	}

}
