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
	@DisplayName("Admin puede agregar un nuevo cliente con clave provisoria y flag debeCambiarPassword activado")
	void adminPuedeCrearCliente() throws Exception {
		String json = """
			{
				"nombre": "Ana",
				"apellido": "López",
				"email": "ana.lopez@cliente.com",
				"passwordProvisoria": "provisoria123"
			}
			""";

		mockMvc.perform(post("/api/admin/clientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.nombre", is("Ana")))
			.andExpect(jsonPath("$.apellido", is("López")))
			.andExpect(jsonPath("$.email", is("ana.lopez@cliente.com")))
			.andExpect(jsonPath("$.rol", is("CLIENTE")))
			.andExpect(jsonPath("$.debeCambiarPassword", is(true)))
			.andExpect(jsonPath("$.password").doesNotExist());

		Usuario guardado = usuarioRepository.findByEmail("ana.lopez@cliente.com").orElseThrow();
		assertThat(guardado.getPasswordHash()).startsWith("$2a$");
		assertThat(guardado.isDebeCambiarPassword()).isTrue();
		assertThat(passwordEncoder.matches("provisoria123", guardado.getPasswordHash())).isTrue();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede listar los clientes registrados en el sistema")
	void adminPuedeListarClientes() throws Exception {
		Usuario cliente = new Usuario();
		cliente.setNombre("Pedro");
		cliente.setApellido("Picapiedra");
		cliente.setEmail("pedro@cliente.com");
		cliente.setPasswordHash(passwordEncoder.encode("clave123"));
		cliente.setRol(Rol.CLIENTE);
		cliente.setActivo(true);
		cliente.setDebeCambiarPassword(true);
		usuarioRepository.save(cliente);

		mockMvc.perform(get("/api/admin/clientes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].email", is("pedro@cliente.com")))
			.andExpect(jsonPath("$[0].debeCambiarPassword", is(true)));
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
	@DisplayName("Admin puede asignar viaje asociando un cliente y el viaje queda vinculado exitosamente")
	void adminPuedeAsignarViajeConClienteAsociado() throws Exception {
		Usuario cliente = new Usuario();
		cliente.setNombre("Empresa");
		cliente.setApellido("Acme");
		cliente.setEmail("acme@test.com");
		cliente.setPasswordHash(passwordEncoder.encode("clave123"));
		cliente.setRol(Rol.CLIENTE);
		cliente.setActivo(true);
		cliente = usuarioRepository.save(cliente);

		String json = """
			{
				"camionId": %d,
				"choferId": %d,
				"clienteId": %d,
				"origenNombre": "Buenos Aires (Terminal Retiro)",
				"origenLat": -34.5888,
				"origenLon": -58.3750,
				"destinoNombre": "Rosario (Puerto)",
				"destinoLat": -32.9468,
				"destinoLon": -60.6393,
				"descripcionCarga": "Carga de alimentos no perecederos",
				"iniciarInmediatamente": true
			}
			""".formatted(camion.getId(), chofer.getId(), cliente.getId());

		mockMvc.perform(post("/api/admin/viajes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.codigoSeguimiento", notNullValue()))
			.andExpect(jsonPath("$.estado", is("EN_CURSO")))
			.andExpect(jsonPath("$.choferNombre", is("Roberto Galán")))
			.andExpect(jsonPath("$.camionPatente", is("AD999ZZ")));

		var viajes = viajeRepository.findAll();
		assertThat(viajes).hasSize(1);
		assertThat(viajes.get(0).getCliente()).isNotNull();
		assertThat(viajes.get(0).getCliente().getId()).isEqualTo(cliente.getId());
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
	@WithMockUser(roles = "ADMIN")
	@DisplayName("No permite asignar un viaje a un chofer que ya tiene un viaje activo en curso")
	void adminNoPuedeAsignarViajeAChoferOcupado() throws Exception {
		adminPuedeAsignarViajeEIniciarSimulacion();

		Camion camion2 = new Camion();
		camion2.setPatente("AE111ZZ");
		camion2.setMarca("Volvo");
		camion2.setModelo("FH540");
		camion2.setAnio(2024);
		camion2.setCapacidadKg(30000);
		camion2.setActivo(true);
		camion2 = camionRepository.save(camion2);

		String jsonSegundo = """
			{
				"camionId": %d,
				"choferId": %d,
				"origenNombre": "Rosario",
				"origenLat": -32.95,
				"origenLon": -60.65,
				"destinoNombre": "Córdoba",
				"destinoLat": -31.42,
				"destinoLon": -64.18,
				"descripcionCarga": "Granos",
				"iniciarInmediatamente": true
			}
			""".formatted(camion2.getId(), chofer.getId());

		mockMvc.perform(post("/api/admin/viajes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonSegundo))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail", is("El chofer seleccionado ya tiene un viaje activo en curso.")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Admin puede finalizar un viaje activo")
	void adminPuedeFinalizarViajeActivo() throws Exception {
		adminPuedeAsignarViajeEIniciarSimulacion();

		String listadoResp = mockMvc.perform(get("/api/admin/viajes/activos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andReturn().getResponse().getContentAsString();

		Long viajeId = com.jayway.jsonpath.JsonPath.parse(listadoResp).read("$[0].viajeId", Integer.class).longValue();

		mockMvc.perform(post("/api/admin/viajes/" + viajeId + "/finalizar"))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/viajes/activos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Generar múltiples viajes simulados asigna choferes y camiones únicos sin duplicar chofer en ruta")
	void generarMultiplesDemosAsignaChoferesDistintos() throws Exception {
		mockMvc.perform(post("/api/admin/simulacion/crear-demo"))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/admin/simulacion/crear-demo"))
			.andExpect(status().isCreated());

		String activos = mockMvc.perform(get("/api/admin/viajes/activos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andReturn().getResponse().getContentAsString();

		String chofer1 = com.jayway.jsonpath.JsonPath.parse(activos).read("$[0].choferNombre", String.class);
		String chofer2 = com.jayway.jsonpath.JsonPath.parse(activos).read("$[1].choferNombre", String.class);
		assertThat(chofer1).isNotEqualTo(chofer2);
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

		mockMvc.perform(post("/api/admin/clientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/clientes"))
			.andExpect(status().isForbidden());
	}

}
