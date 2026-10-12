package um.edu.ar.omnitrucks.viaje.adapters.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

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
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Tests de integración del controlador Web del Portal de Cliente y Seguimiento de Envíos.
 * Valida reglas estrictas de autorización, aislamiento de información entre clientes y prevención de over-fetching.
 */
@SpringBootTest
@Transactional
class ClienteSeguimientoControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ViajeRepository viajeRepository;

	@Autowired
	private CamionRepository camionRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PosicionRepository posicionRepository;

	private Usuario clienteA;
	private Usuario clienteB;
	private Usuario chofer;
	private Viaje viajeClienteA;
	private Viaje viajeClienteB;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		posicionRepository.deleteAllInBatch();
		viajeRepository.deleteAllInBatch();
		camionRepository.deleteAll();
		usuarioRepository.deleteAllInBatch();

		clienteA = new Usuario();
		clienteA.setEmail("cliente.a@omnitrucks.com");
		clienteA.setPasswordHash("$2a$10$dummyHash");
		clienteA.setNombre("Empresa");
		clienteA.setApellido("Alfa");
		clienteA.setRol(Rol.CLIENTE);
		clienteA.setActivo(true);
		clienteA = usuarioRepository.save(clienteA);

		clienteB = new Usuario();
		clienteB.setEmail("cliente.b@omnitrucks.com");
		clienteB.setPasswordHash("$2a$10$dummyHash");
		clienteB.setNombre("Distribuidora");
		clienteB.setApellido("Beta");
		clienteB.setRol(Rol.CLIENTE);
		clienteB.setActivo(true);
		clienteB = usuarioRepository.save(clienteB);

		chofer = new Usuario();
		chofer.setEmail("chofer@omnitrucks.com");
		chofer.setPasswordHash("$2a$10$dummyHash");
		chofer.setNombre("Mario");
		chofer.setApellido("Baracus");
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);
		chofer = usuarioRepository.save(chofer);

		Usuario admin = new Usuario();
		admin.setEmail("admin@omnitrucks.com");
		admin.setPasswordHash("$2a$10$dummyHash");
		admin.setNombre("Martin");
		admin.setApellido("Admin");
		admin.setRol(Rol.ADMIN);
		admin.setActivo(true);
		usuarioRepository.save(admin);

		Camion camion1 = new Camion();
		camion1.setPatente("AA111AA");
		camion1.setMarca("Scania");
		camion1.setModelo("R450");
		camion1.setAnio(2021);
		camion1.setCapacidadKg(30000);
		camion1.setActivo(true);
		camion1 = camionRepository.save(camion1);

		Camion camion2 = new Camion();
		camion2.setPatente("BB222BB");
		camion2.setMarca("Volvo");
		camion2.setModelo("FH16");
		camion2.setAnio(2022);
		camion2.setCapacidadKg(35000);
		camion2.setActivo(true);
		camion2 = camionRepository.save(camion2);

		// Viaje asignado a Cliente A
		viajeClienteA = new Viaje();
		viajeClienteA.setCodigoSeguimiento("TRK-ALFA001");
		viajeClienteA.setCamion(camion1);
		viajeClienteA.setChofer(chofer);
		viajeClienteA.setCliente(clienteA);
		viajeClienteA.setOrigenNombre("Mendoza");
		viajeClienteA.setOrigenLat(-32.889);
		viajeClienteA.setOrigenLon(-68.845);
		viajeClienteA.setDestinoNombre("Buenos Aires");
		viajeClienteA.setDestinoLat(-34.603);
		viajeClienteA.setDestinoLon(-58.381);
		viajeClienteA.setDescripcionCarga("Vinos Finos");
		viajeClienteA.setEstado(EstadoViaje.EN_CURSO);
		viajeClienteA = viajeRepository.save(viajeClienteA);

		// Posición actual para viaje de Cliente A
		Posicion pos = new Posicion();
		pos.setViaje(viajeClienteA);
		pos.setLatitud(-33.200);
		pos.setLongitud(-66.100);
		pos.setVelocidadKmh(85.0);
		pos.setRumboGrados(92.0);
		pos.setRegistradoEn(Instant.now());
		posicionRepository.save(pos);

		// Viaje asignado a Cliente B
		viajeClienteB = new Viaje();
		viajeClienteB.setCodigoSeguimiento("TRK-BETA002");
		viajeClienteB.setCamion(camion2);
		viajeClienteB.setChofer(chofer);
		viajeClienteB.setCliente(clienteB);
		viajeClienteB.setOrigenNombre("Rosario");
		viajeClienteB.setOrigenLat(-32.950);
		viajeClienteB.setOrigenLon(-60.650);
		viajeClienteB.setDestinoNombre("Cordoba");
		viajeClienteB.setDestinoLat(-31.420);
		viajeClienteB.setDestinoLon(-64.180);
		viajeClienteB.setDescripcionCarga("Cereales");
		viajeClienteB.setEstado(EstadoViaje.PROGRAMADO);
		viajeClienteB = viajeRepository.save(viajeClienteB);
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo} como cliente asignado retorna 200 y datos públicos sin over-fetching")
	@WithMockUser(username = "cliente.a@omnitrucks.com", roles = "CLIENTE")
	void consultarPorCodigo_comoClienteAsignado_retorna200YDatosPublicos() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-ALFA001"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.codigoSeguimiento").value("TRK-ALFA001"))
			.andExpect(jsonPath("$.estado").value("EN_CURSO"))
			.andExpect(jsonPath("$.descripcionCarga").value("Vinos Finos"))
			.andExpect(jsonPath("$.origen.nombre").value("Mendoza"))
			.andExpect(jsonPath("$.destino.nombre").value("Buenos Aires"))
			.andExpect(jsonPath("$.vehiculo.patente").value("AA111AA"))
			.andExpect(jsonPath("$.vehiculo.marca").value("Scania"))
			.andExpect(jsonPath("$.chofer.nombre").value("Mario Baracus"))
			.andExpect(jsonPath("$.ubicacionActual.latitud").value(-33.200))
			.andExpect(jsonPath("$.ubicacionActual.longitud").value(-66.100))
			// Comprueba que datos sensibles internos NO existan en la respuesta (sin over-fetching)
			.andExpect(jsonPath("$.id").doesNotExist())
			.andExpect(jsonPath("$.chofer.email").doesNotExist())
			.andExpect(jsonPath("$.vehiculo.id").doesNotExist())
			.andExpect(jsonPath("$.vehiculo.capacidadKg").doesNotExist());
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo} consultando envío de otro cliente retorna 403 Forbidden")
	@WithMockUser(username = "cliente.a@omnitrucks.com", roles = "CLIENTE")
	void consultarPorCodigo_comoOtroCliente_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-BETA002"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.title").value("Acceso denegado"))
			.andExpect(jsonPath("$.detail").value("No tiene permisos para consultar este envío."));
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo} con código inexistente retorna 404 Not Found")
	@WithMockUser(username = "cliente.a@omnitrucks.com", roles = "CLIENTE")
	void consultarPorCodigo_codigoInexistente_retorna404NotFound() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-INEXISTENTE"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Recurso no encontrado"));
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo} como ADMIN permite supervisar el envío de cualquier cliente")
	@WithMockUser(username = "admin@omnitrucks.com", roles = "ADMIN")
	void consultarPorCodigo_comoAdmin_retorna200() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-ALFA001"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.codigoSeguimiento").value("TRK-ALFA001"));
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo} como CHOFER es denegado con 403 Forbidden")
	@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
	void consultarPorCodigo_comoChofer_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-ALFA001"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo} sin autenticación es denegado con 403 Forbidden")
	void consultarPorCodigo_sinAutenticacion_retornaForbidden() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-ALFA001"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/cliente/mis-envios retorna únicamente los envíos asignados al cliente autenticado")
	@WithMockUser(username = "cliente.a@omnitrucks.com", roles = "CLIENTE")
	void listarMisEnvios_comoCliente_retornaSoloEnviosAsignados() throws Exception {
		mockMvc.perform(get("/api/cliente/mis-envios"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].codigoSeguimiento").value("TRK-ALFA001"))
			.andExpect(jsonPath("$[0].origenNombre").value("Mendoza"))
			.andExpect(jsonPath("$[0].destinoNombre").value("Buenos Aires"));
	}

	@Test
	@DisplayName("GET /api/cliente/seguimiento/{codigo}/ubicacion retorna la posición en vivo para el cliente asignado")
	@WithMockUser(username = "cliente.a@omnitrucks.com", roles = "CLIENTE")
	void consultarUbicacionEnVivo_comoClienteAsignado_retorna200() throws Exception {
		mockMvc.perform(get("/api/cliente/seguimiento/TRK-ALFA001/ubicacion"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.codigoSeguimiento").value("TRK-ALFA001"))
			.andExpect(jsonPath("$.latitud").value(-33.200))
			.andExpect(jsonPath("$.longitud").value(-66.100))
			.andExpect(jsonPath("$.velocidadKmh").value(85.0));
	}

}
