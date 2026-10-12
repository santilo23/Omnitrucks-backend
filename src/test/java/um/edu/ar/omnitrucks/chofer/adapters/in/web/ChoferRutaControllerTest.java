package um.edu.ar.omnitrucks.chofer.adapters.in.web;

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
 * Tests de integración para el Portal de Choferes y visualización de rutas restringidas.
 * Valida que el chofer solo obtenga la información y telemetría de su vehículo asignado,
 * y que no pueda acceder a viajes de otros choferes ni al panel de administración.
 */
@SpringBootTest
@Transactional
class ChoferRutaControllerTest {

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

	private Usuario choferA;
	private Usuario choferB;
	private Usuario choferSinViaje;
	private Usuario admin;
	private Usuario cliente;
	private Viaje viajeChoferA;
	private Viaje viajeChoferB;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		posicionRepository.deleteAllInBatch();
		viajeRepository.deleteAllInBatch();
		camionRepository.deleteAll();
		usuarioRepository.deleteAllInBatch();

		choferA = new Usuario();
		choferA.setEmail("chofer.a@omnitrucks.com");
		choferA.setPasswordHash("$2a$10$dummyHash");
		choferA.setNombre("Mario");
		choferA.setApellido("Baracus");
		choferA.setRol(Rol.CHOFER);
		choferA.setActivo(true);
		choferA = usuarioRepository.save(choferA);

		choferB = new Usuario();
		choferB.setEmail("chofer.b@omnitrucks.com");
		choferB.setPasswordHash("$2a$10$dummyHash");
		choferB.setNombre("Carlos");
		choferB.setApellido("Sainz");
		choferB.setRol(Rol.CHOFER);
		choferB.setActivo(true);
		choferB = usuarioRepository.save(choferB);

		choferSinViaje = new Usuario();
		choferSinViaje.setEmail("chofer.libre@omnitrucks.com");
		choferSinViaje.setPasswordHash("$2a$10$dummyHash");
		choferSinViaje.setNombre("Juan");
		choferSinViaje.setApellido("Libre");
		choferSinViaje.setRol(Rol.CHOFER);
		choferSinViaje.setActivo(true);
		choferSinViaje = usuarioRepository.save(choferSinViaje);

		admin = new Usuario();
		admin.setEmail("admin@omnitrucks.com");
		admin.setPasswordHash("$2a$10$dummyHash");
		admin.setNombre("Martin");
		admin.setApellido("Admin");
		admin.setRol(Rol.ADMIN);
		admin.setActivo(true);
		admin = usuarioRepository.save(admin);

		cliente = new Usuario();
		cliente.setEmail("cliente@omnitrucks.com");
		cliente.setPasswordHash("$2a$10$dummyHash");
		cliente.setNombre("Empresa");
		cliente.setApellido("Cliente");
		cliente.setRol(Rol.CLIENTE);
		cliente.setActivo(true);
		cliente = usuarioRepository.save(cliente);

		Camion camionA = new Camion();
		camionA.setPatente("AA111AA");
		camionA.setMarca("Scania");
		camionA.setModelo("R450");
		camionA.setAnio(2022);
		camionA.setCapacidadKg(35000);
		camionA.setActivo(true);
		camionA = camionRepository.save(camionA);

		Camion camionB = new Camion();
		camionB.setPatente("BB222BB");
		camionB.setMarca("Volvo");
		camionB.setModelo("FH16");
		camionB.setAnio(2023);
		camionB.setCapacidadKg(38000);
		camionB.setActivo(true);
		camionB = camionRepository.save(camionB);

		// Viaje de Chofer A
		viajeChoferA = new Viaje();
		viajeChoferA.setCodigoSeguimiento("TRK-CHOFER-A");
		viajeChoferA.setCamion(camionA);
		viajeChoferA.setChofer(choferA);
		viajeChoferA.setOrigenNombre("Mendoza");
		viajeChoferA.setOrigenLat(-32.889);
		viajeChoferA.setOrigenLon(-68.845);
		viajeChoferA.setDestinoNombre("Buenos Aires");
		viajeChoferA.setDestinoLat(-34.603);
		viajeChoferA.setDestinoLon(-58.381);
		viajeChoferA.setDescripcionCarga("Mercadería General");
		viajeChoferA.setEstado(EstadoViaje.EN_CURSO);
		viajeChoferA = viajeRepository.save(viajeChoferA);

		Posicion posA = new Posicion();
		posA.setViaje(viajeChoferA);
		posA.setLatitud(-33.200);
		posA.setLongitud(-66.100);
		posA.setVelocidadKmh(82.0);
		posA.setRumboGrados(90.0);
		posA.setRegistradoEn(Instant.now());
		posicionRepository.save(posA);

		// Viaje de Chofer B
		viajeChoferB = new Viaje();
		viajeChoferB.setCodigoSeguimiento("TRK-CHOFER-B");
		viajeChoferB.setCamion(camionB);
		viajeChoferB.setChofer(choferB);
		viajeChoferB.setOrigenNombre("Cordoba");
		viajeChoferB.setOrigenLat(-31.420);
		viajeChoferB.setOrigenLon(-64.180);
		viajeChoferB.setDestinoNombre("Rosario");
		viajeChoferB.setDestinoLat(-32.950);
		viajeChoferB.setDestinoLon(-60.650);
		viajeChoferB.setDescripcionCarga("Autopartes");
		viajeChoferB.setEstado(EstadoViaje.EN_CURSO);
		viajeChoferB = viajeRepository.save(viajeChoferB);
	}

	@Test
	@DisplayName("GET /api/chofer/ruta-activa retorna la ruta y únicamente el vehículo asociado al chofer en sesión")
	@WithMockUser(username = "chofer.a@omnitrucks.com", roles = "CHOFER")
	void consultarRutaActiva_comoChoferConViaje_retorna200YSuCamion() throws Exception {
		mockMvc.perform(get("/api/chofer/ruta-activa"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.viajeId").value(viajeChoferA.getId()))
			.andExpect(jsonPath("$.codigoSeguimiento").value("TRK-CHOFER-A"))
			.andExpect(jsonPath("$.estado").value("EN_CURSO"))
			// Valida que el camión retornado sea exclusivamente el de Chofer A
			.andExpect(jsonPath("$.vehiculo.patente").value("AA111AA"))
			.andExpect(jsonPath("$.vehiculo.marca").value("Scania"))
			.andExpect(jsonPath("$.origen.nombre").value("Mendoza"))
			.andExpect(jsonPath("$.destino.nombre").value("Buenos Aires"))
			.andExpect(jsonPath("$.ultimaUbicacion.latitud").value(-33.200))
			.andExpect(jsonPath("$.recorrido", hasSize(1)));
	}

	@Test
	@DisplayName("GET /api/chofer/ruta-activa sin viajes activos retorna 204 No Content")
	@WithMockUser(username = "chofer.libre@omnitrucks.com", roles = "CHOFER")
	void consultarRutaActiva_comoChoferSinViaje_retorna204NoContent() throws Exception {
		mockMvc.perform(get("/api/chofer/ruta-activa"))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("GET /api/chofer/viajes/{viajeId}/ruta consultando el viaje de otro chofer es denegado con 403 Forbidden")
	@WithMockUser(username = "chofer.b@omnitrucks.com", roles = "CHOFER")
	void consultarRutaPorViaje_comoOtroChofer_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/chofer/viajes/" + viajeChoferA.getId() + "/ruta"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.title").value("Acceso denegado"))
			.andExpect(jsonPath("$.detail").value("No tiene permisos para acceder a la ruta de otro chofer."));
	}

	@Test
	@DisplayName("GET /api/chofer/viajes/{viajeId}/ruta como ADMIN permite supervisar la ruta")
	@WithMockUser(username = "admin@omnitrucks.com", roles = "ADMIN")
	void consultarRutaPorViaje_comoAdmin_retorna200() throws Exception {
		mockMvc.perform(get("/api/chofer/viajes/" + viajeChoferA.getId() + "/ruta"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.viajeId").value(viajeChoferA.getId()))
			.andExpect(jsonPath("$.vehiculo.patente").value("AA111AA"));
	}

	@Test
	@DisplayName("GET /api/chofer/ruta-activa como CLIENTE es denegado con 403 Forbidden")
	@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
	void consultarRutaActiva_comoCliente_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/chofer/ruta-activa"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/chofer/ruta-activa sin autenticación es denegado con 403 Forbidden")
	void consultarRutaActiva_sinAutenticar_retornaForbidden() throws Exception {
		mockMvc.perform(get("/api/chofer/ruta-activa"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/chofer/mis-viajes retorna únicamente los viajes asignados a ese chofer")
	@WithMockUser(username = "chofer.a@omnitrucks.com", roles = "CHOFER")
	void listarMisViajes_comoChofer_retornaSoloSusViajes() throws Exception {
		mockMvc.perform(get("/api/chofer/mis-viajes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].viajeId").value(viajeChoferA.getId()))
			.andExpect(jsonPath("$[0].patenteCamion").value("AA111AA"));
	}

	@Test
	@DisplayName("Un CHOFER no puede acceder al panel de administración de camiones (403 Forbidden)")
	@WithMockUser(username = "chofer.a@omnitrucks.com", roles = "CHOFER")
	void chofer_intentandoAccederAPanelAdmin_retorna403Forbidden() throws Exception {
		mockMvc.perform(get("/api/admin/flota"))
			.andExpect(status().isForbidden());
	}

}
