package um.edu.ar.omnitrucks.mapa;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
 * ETAPA 5: Tests de integración finales E2E para políticas de acceso y renderizado del mapa.
 *
 * Valida:
 * 1. Control de Acceso basado en Roles (RBAC) estricto para ADMIN, CHOFER y CLIENTE sobre todos los endpoints del mapa.
 * 2. Ausencia de fugas de datos (Over-fetching) en los DTOs inmutables enviados al frontend.
 * 3. Bloqueo para peticiones no autenticadas.
 */
@SpringBootTest
@Transactional
class MapaSeguridadIntegracionTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CamionRepository camionRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ViajeRepository viajeRepository;

	@Autowired
	private PosicionRepository posicionRepository;

	private Usuario admin;
	private Usuario chofer;
	private Usuario cliente;
	private Camion camion;
	private Viaje viajeActivo;
	private Posicion ultimaPosicion;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		posicionRepository.deleteAllInBatch();
		viajeRepository.deleteAllInBatch();
		camionRepository.deleteAll();
		usuarioRepository.deleteAllInBatch();

		admin = new Usuario();
		admin.setEmail("admin@omnitrucks.com");
		admin.setPasswordHash("$2a$10$dummyHashAdmin");
		admin.setNombre("Super");
		admin.setApellido("Admin");
		admin.setRol(Rol.ADMIN);
		admin.setActivo(true);
		admin = usuarioRepository.save(admin);

		chofer = new Usuario();
		chofer.setEmail("chofer@omnitrucks.com");
		chofer.setPasswordHash("$2a$10$dummyHashChofer");
		chofer.setNombre("Esteban");
		chofer.setApellido("Quito");
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);
		chofer = usuarioRepository.save(chofer);

		cliente = new Usuario();
		cliente.setEmail("cliente@omnitrucks.com");
		cliente.setPasswordHash("$2a$10$dummyHashCliente");
		cliente.setNombre("Acme");
		cliente.setApellido("Corp");
		cliente.setRol(Rol.CLIENTE);
		cliente.setActivo(true);
		cliente = usuarioRepository.save(cliente);

		camion = new Camion();
		camion.setPatente("AA123BB");
		camion.setMarca("Volvo");
		camion.setModelo("FH 540");
		camion.setAnio(2023);
		camion.setCapacidadKg(35000);
		camion.setActivo(true);
		camion = camionRepository.save(camion);

		viajeActivo = new Viaje();
		viajeActivo.setCamion(camion);
		viajeActivo.setChofer(chofer);
		viajeActivo.setCliente(cliente);
		viajeActivo.setCodigoSeguimiento("TRK-ETAPA5-999");
		viajeActivo.setEstado(EstadoViaje.EN_CURSO);
		viajeActivo.setOrigenNombre("Buenos Aires");
		viajeActivo.setOrigenLat(-34.6037);
		viajeActivo.setOrigenLon(-58.3816);
		viajeActivo.setDestinoNombre("Córdoba");
		viajeActivo.setDestinoLat(-31.4201);
		viajeActivo.setDestinoLon(-64.1888);
		viajeActivo.setDescripcionCarga("Electrónica y componentes de alta gama");
		viajeActivo.setSalidaProgramada(Instant.now().minusSeconds(7200));
		viajeActivo.setSalidaReal(Instant.now().minusSeconds(7000));
		viajeActivo = viajeRepository.save(viajeActivo);

		ultimaPosicion = new Posicion();
		ultimaPosicion.setViaje(viajeActivo);
		ultimaPosicion.setLatitud(-33.0);
		ultimaPosicion.setLongitud(-60.0);
		ultimaPosicion.setVelocidadKmh(85.0);
		ultimaPosicion.setRumboGrados(315.0);
		ultimaPosicion.setPrecisionM(5.0);
		ultimaPosicion.setRegistradoEn(Instant.now().minusSeconds(10));
		ultimaPosicion.setRecibidoEn(Instant.now());
		ultimaPosicion = posicionRepository.save(ultimaPosicion);
	}

	@Nested
	@DisplayName("1. Rol ADMIN: Acceso integral de supervisión y gestión del mapa")
	class RolAdminTests {

		@Test
		@WithMockUser(username = "admin@omnitrucks.com", roles = "ADMIN")
		@DisplayName("Admin puede consultar el mapa global con las posiciones de toda la flota")
		void adminPuedeAccederMapaGlobalFlota() throws Exception {
			mockMvc.perform(get("/api/viajes/activos/posiciones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].patente", is("AA123BB")))
				.andExpect(jsonPath("$[0].destinoNombre", is("Córdoba")))
				.andExpect(jsonPath("$[0].posicion.latitud", is(-33.0)))
				.andExpect(jsonPath("$[0].posicion.longitud", is(-60.0)));
		}

		@Test
		@WithMockUser(username = "admin@omnitrucks.com", roles = "ADMIN")
		@DisplayName("Admin puede listar los viajes activos y el panel de flota")
		void adminPuedeAccederViajesActivosYPanelFlota() throws Exception {
			mockMvc.perform(get("/api/viajes/activos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

			mockMvc.perform(get("/api/admin/flota"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
		}

		@Test
		@WithMockUser(username = "admin@omnitrucks.com", roles = "ADMIN")
		@DisplayName("Admin puede consultar la traza cronológica (recorrido) para dibujar la Polyline")
		void adminPuedeAccederRecorrido() throws Exception {
			mockMvc.perform(get("/api/viajes/" + viajeActivo.getId() + "/recorrido"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].latitud", is(-33.0)));
		}
	}

	@Nested
	@DisplayName("2. Rol CHOFER: Acceso restringido únicamente a su ruta y vehículo asignado")
	class RolChoferTests {

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("Chofer puede ver su propia ruta asignada, destino y telemetría")
		void choferPuedeVerSuRutaActiva() throws Exception {
			mockMvc.perform(get("/api/chofer/ruta-activa"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vehiculo.patente", is("AA123BB")))
				.andExpect(jsonPath("$.destino.nombre", is("Córdoba")))
				.andExpect(jsonPath("$.descripcionCarga", is("Electrónica y componentes de alta gama")))
				.andExpect(jsonPath("$.ultimaUbicacion.latitud", is(-33.0)));
		}

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("Chofer puede reportar posiciones GPS para su viaje")
		void choferPuedeEnviarPosicionGps() throws Exception {
			String json = """
				[
					{
						"latitud": -32.95,
						"longitud": -60.10,
						"velocidadKmh": 80.0,
						"rumboGrados": 310.0,
						"precisionM": 4.0,
						"registradoEn": "2026-10-12T12:00:00Z"
					}
				]
				""";

			mockMvc.perform(post("/api/viajes/" + viajeActivo.getId() + "/posiciones")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].latitud", is(-32.95)));
		}

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("Chofer tiene ACCESO DENEGADO (403) al mapa global de toda la flota corporativa")
		void choferNoPuedeVerMapaGlobalDeFlota() throws Exception {
			mockMvc.perform(get("/api/viajes/activos/posiciones"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("Chofer tiene ACCESO DENEGADO (403) al listado corporativo de viajes")
		void choferNoPuedeVerViajesCorporativos() throws Exception {
			mockMvc.perform(get("/api/viajes"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("Chofer tiene ACCESO DENEGADO (403) al panel de administración de flota")
		void choferNoPuedeVerPanelAdminFlota() throws Exception {
			mockMvc.perform(get("/api/admin/flota"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("Chofer tiene ACCESO DENEGADO (403) a los envíos del portal de clientes")
		void choferNoPuedeVerPortalClientes() throws Exception {
			mockMvc.perform(get("/api/cliente/mis-envios"))
				.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("3. Rol CLIENTE: Acceso exclusivo a su envío asignado mediante código de seguimiento")
	class RolClienteTests {

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("Cliente puede consultar la ubicación en tiempo real de su envío asignado")
		void clientePuedeVerUbicacionEnVivoDeSuEnvio() throws Exception {
			mockMvc.perform(get("/api/cliente/seguimiento/" + viajeActivo.getCodigoSeguimiento() + "/ubicacion"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.codigoSeguimiento", is("TRK-ETAPA5-999")))
				.andExpect(jsonPath("$.latitud", is(-33.0)))
				.andExpect(jsonPath("$.longitud", is(-60.0)));
		}

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("Cliente tiene ACCESO DENEGADO (403) al mapa global de toda la flota")
		void clienteNoPuedeVerMapaGlobalDeFlota() throws Exception {
			mockMvc.perform(get("/api/viajes/activos/posiciones"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("Cliente tiene ACCESO DENEGADO (403) a la gestión corporativa de viajes")
		void clienteNoPuedeVerViajesCorporativos() throws Exception {
			mockMvc.perform(get("/api/viajes"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("Cliente tiene ACCESO DENEGADO (403) al panel de administración de flota")
		void clienteNoPuedeVerPanelAdminFlota() throws Exception {
			mockMvc.perform(get("/api/admin/flota"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("Cliente tiene ACCESO DENEGADO (403) al portal y rutas de choferes")
		void clienteNoPuedeVerRutasDeChofer() throws Exception {
			mockMvc.perform(get("/api/chofer/ruta-activa"))
				.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("Cliente tiene ACCESO DENEGADO (403) a la ingesta de posiciones GPS")
		void clienteNoPuedeEnviarPosicionesGps() throws Exception {
			mockMvc.perform(post("/api/viajes/" + viajeActivo.getId() + "/posiciones")
					.contentType(MediaType.APPLICATION_JSON)
					.content("[]"))
				.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("4. Usuario No Autenticado: Bloqueo en todos los endpoints privados del mapa")
	class UsuarioNoAutenticadoTests {

		@Test
		@DisplayName("Petición anónima al mapa global de flota es bloqueada (401 o 403)")
		void anonimoBloqueadoEnMapaGlobal() throws Exception {
			mockMvc.perform(get("/api/viajes/activos/posiciones"))
				.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("Petición anónima a la ruta del chofer es bloqueada (401 o 403)")
		void anonimoBloqueadoEnRutaChofer() throws Exception {
			mockMvc.perform(get("/api/chofer/ruta-activa"))
				.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("Petición anónima a la telemetría del cliente es bloqueada (401 o 403)")
		void anonimoBloqueadoEnSeguimientoCliente() throws Exception {
			mockMvc.perform(get("/api/cliente/seguimiento/" + viajeActivo.getCodigoSeguimiento() + "/ubicacion"))
				.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("5. Validación de Over-fetching y Privacidad de Datos en los DTOs record")
	class OverFetchingValidationTests {

		@Test
		@WithMockUser(username = "cliente@omnitrucks.com", roles = "CLIENTE")
		@DisplayName("El DTO de seguimiento del cliente oculta contraseñas, emails y datos sensibles del chofer y flota")
		void clienteDtoNoExponeDatosSensibles() throws Exception {
			mockMvc.perform(get("/api/cliente/seguimiento/" + viajeActivo.getCodigoSeguimiento()))
				.andExpect(status().isOk())
				// Datos públicos permitidos
				.andExpect(jsonPath("$.codigoSeguimiento", is("TRK-ETAPA5-999")))
				.andExpect(jsonPath("$.vehiculo.patente", is("AA123BB")))
				.andExpect(jsonPath("$.vehiculo.marca", is("Volvo")))
				.andExpect(jsonPath("$.vehiculo.modelo", is("FH 540")))
				.andExpect(jsonPath("$.chofer.nombre", is("Esteban Quito")))
				// Protecciones de privacidad (NO fugas de información)
				.andExpect(jsonPath("$.chofer.email").doesNotExist())
				.andExpect(jsonPath("$.chofer.password").doesNotExist())
				.andExpect(jsonPath("$.chofer.passwordHash").doesNotExist())
				.andExpect(jsonPath("$.chofer.rol").doesNotExist())
				.andExpect(jsonPath("$.vehiculo.capacidadKg").doesNotExist())
				.andExpect(jsonPath("$.vehiculo.id").doesNotExist());
		}

		@Test
		@WithMockUser(username = "chofer@omnitrucks.com", roles = "CHOFER")
		@DisplayName("El DTO de ruta del chofer no contiene credenciales de usuarios ni datos de terceros")
		void choferDtoNoExponeCredenciales() throws Exception {
			mockMvc.perform(get("/api/chofer/ruta-activa"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vehiculo.patente", is("AA123BB")))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andExpect(jsonPath("$.cliente").doesNotExist());
		}

		@Test
		@WithMockUser(username = "admin@omnitrucks.com", roles = "ADMIN")
		@DisplayName("El DTO de ubicaciones activas del mapa no expone passwordHash de usuarios")
		void ubicacionesActivasNoExponePasswordHash() throws Exception {
			mockMvc.perform(get("/api/viajes/activos/posiciones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].passwordHash").doesNotExist())
				.andExpect(jsonPath("$[0].posicion.passwordHash").doesNotExist());
		}
	}

}
