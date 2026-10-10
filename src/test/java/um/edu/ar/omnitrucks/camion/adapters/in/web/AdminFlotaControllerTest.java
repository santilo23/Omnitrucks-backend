package um.edu.ar.omnitrucks.camion.adapters.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Tests de integración del controlador Web del Panel de Administración de Flota.
 * Valida filtros, métricas de resumen y reglas de autorización RBAC (solo ADMIN).
 */
@SpringBootTest
@Transactional
class AdminFlotaControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CamionRepository camionRepository;

	@Autowired
	private ViajeRepository viajeRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	private Camion camionDisponible;
	private Camion camionEnTransito;
	private Camion camionInactivo;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		viajeRepository.deleteAllInBatch();
		camionRepository.deleteAll();
		usuarioRepository.deleteAllInBatch();

		Usuario chofer = new Usuario();
		chofer.setEmail("chofer.flota@omnitrucks.com");
		chofer.setPasswordHash("$2a$10$dummyHashValue");
		chofer.setNombre("Carlos");
		chofer.setApellido("Perez");
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);
		chofer = usuarioRepository.save(chofer);

		// Camión 1: Activo y disponible (sin viajes activos)
		camionDisponible = new Camion();
		camionDisponible.setPatente("AA111AA");
		camionDisponible.setMarca("Scania");
		camionDisponible.setModelo("R450");
		camionDisponible.setAnio(2021);
		camionDisponible.setCapacidadKg(30000);
		camionDisponible.setActivo(true);
		camionDisponible = camionRepository.save(camionDisponible);

		// Camión 2: Activo y en tránsito (con viaje en curso)
		camionEnTransito = new Camion();
		camionEnTransito.setPatente("BB222BB");
		camionEnTransito.setMarca("Volvo");
		camionEnTransito.setModelo("FH16");
		camionEnTransito.setAnio(2022);
		camionEnTransito.setCapacidadKg(35000);
		camionEnTransito.setActivo(true);
		camionEnTransito = camionRepository.save(camionEnTransito);

		Viaje viaje = new Viaje();
		viaje.setCamion(camionEnTransito);
		viaje.setChofer(chofer);
		viaje.setOrigenNombre("Mendoza");
		viaje.setOrigenLat(-32.889);
		viaje.setOrigenLon(-68.845);
		viaje.setDestinoNombre("Buenos Aires");
		viaje.setDestinoLat(-34.603);
		viaje.setDestinoLon(-58.381);
		viaje.setEstado(EstadoViaje.EN_CURSO);
		viajeRepository.save(viaje);

		// Camión 3: Inactivo (dado de baja lógica)
		camionInactivo = new Camion();
		camionInactivo.setPatente("CC333CC");
		camionInactivo.setMarca("Mercedes");
		camionInactivo.setModelo("Actros");
		camionInactivo.setAnio(2019);
		camionInactivo.setCapacidadKg(28000);
		camionInactivo.setActivo(false);
		camionInactivo = camionRepository.save(camionInactivo);
	}

	@Test
	@DisplayName("GET /api/admin/flota sin filtro retorna todos los camiones con estado operativo ordenados")
	@WithMockUser(roles = "ADMIN")
	void consultarFlota_comoAdmin_sinFiltro_retornaTodosConEstadoOperativo() throws Exception {
		mockMvc.perform(get("/api/admin/flota"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(3)))
			.andExpect(jsonPath("$[0].patente").value("AA111AA"))
			.andExpect(jsonPath("$[0].estadoOperativo").value("DISPONIBLE"))
			.andExpect(jsonPath("$[0].activo").value(true))
			.andExpect(jsonPath("$[1].patente").value("BB222BB"))
			.andExpect(jsonPath("$[1].estadoOperativo").value("EN_TRANSITO"))
			.andExpect(jsonPath("$[1].activo").value(true))
			.andExpect(jsonPath("$[2].patente").value("CC333CC"))
			.andExpect(jsonPath("$[2].estadoOperativo").value("INACTIVO"))
			.andExpect(jsonPath("$[2].activo").value(false));
	}

	@Test
	@DisplayName("GET /api/admin/flota?filtro=ACTIVOS retorna únicamente camiones activos")
	@WithMockUser(roles = "ADMIN")
	void consultarFlota_comoAdmin_filtroActivos_retornaSoloActivos() throws Exception {
		mockMvc.perform(get("/api/admin/flota").param("filtro", "ACTIVOS"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].patente").value("AA111AA"))
			.andExpect(jsonPath("$[0].estadoOperativo").value("DISPONIBLE"))
			.andExpect(jsonPath("$[1].patente").value("BB222BB"))
			.andExpect(jsonPath("$[1].estadoOperativo").value("EN_TRANSITO"));
	}

	@Test
	@DisplayName("GET /api/admin/flota?filtro=INACTIVOS retorna únicamente camiones inactivos")
	@WithMockUser(roles = "ADMIN")
	void consultarFlota_comoAdmin_filtroInactivos_retornaSoloInactivos() throws Exception {
		mockMvc.perform(get("/api/admin/flota").param("filtro", "INACTIVOS"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].patente").value("CC333CC"))
			.andExpect(jsonPath("$[0].estadoOperativo").value("INACTIVO"))
			.andExpect(jsonPath("$[0].activo").value(false));
	}

	@Test
	@DisplayName("GET /api/admin/flota/resumen retorna el balance cuantitativo de la flota")
	@WithMockUser(roles = "ADMIN")
	void obtenerResumen_comoAdmin_retornaMetricasCorrectas() throws Exception {
		mockMvc.perform(get("/api/admin/flota/resumen"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.total").value(3))
			.andExpect(jsonPath("$.activos").value(2))
			.andExpect(jsonPath("$.inactivos").value(1))
			.andExpect(jsonPath("$.disponibles").value(1))
			.andExpect(jsonPath("$.enTransito").value(1));
	}

	@Test
	@DisplayName("GET /api/admin/flota para rol CHOFER es denegado con 403 Forbidden")
	@WithMockUser(roles = "CHOFER")
	void consultarFlota_comoChofer_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/admin/flota"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/admin/flota para rol CLIENTE es denegado con 403 Forbidden")
	@WithMockUser(roles = "CLIENTE")
	void consultarFlota_comoCliente_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/admin/flota"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/admin/flota/resumen para rol CHOFER es denegado con 403 Forbidden")
	@WithMockUser(roles = "CHOFER")
	void obtenerResumen_comoChofer_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/admin/flota/resumen"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/admin/flota sin autenticación es denegado con 403 Forbidden")
	void consultarFlota_sinAutenticar_retornaForbidden() throws Exception {
		mockMvc.perform(get("/api/admin/flota"))
			.andExpect(status().isForbidden());
	}

}
