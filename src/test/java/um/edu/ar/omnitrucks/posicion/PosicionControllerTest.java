package um.edu.ar.omnitrucks.posicion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.CamionRepository;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.UsuarioRepository;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;

/**
 * Ingesta y consulta de las posiciones GPS de un viaje.
 *
 * Es la API que van a usar tanto el simulador de la etapa siguiente como,
 * más adelante, la app del chofer reportando su ubicación real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PosicionControllerTest {

	/** Hora fija de referencia, para no depender del reloj al ordenar. */
	private static final Instant T0 = Instant.parse("2026-09-25T12:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CamionRepository camiones;

	@Autowired
	private UsuarioRepository usuarios;

	@Autowired
	private ViajeRepository viajes;

	@Autowired
	private PosicionRepository posiciones;

	private Camion camion;
	private Usuario chofer;
	private Long viajeEnCursoId;

	@BeforeEach
	void prepararDatos() {
		posiciones.deleteAllInBatch();
		viajes.deleteAllInBatch();
		camiones.deleteAllInBatch();
		usuarios.deleteAllInBatch();

		camion = new Camion();
		camion.setPatente("AB123CD");
		camion.setMarca("Scania");
		camion.setModelo("R450");
		camion = camiones.save(camion);

		chofer = new Usuario();
		chofer.setEmail("chofer@omnitrucks.com");
		chofer.setPasswordHash("no-importa-todavia");
		chofer.setNombre("Juan");
		chofer.setApellido("Pérez");
		chofer.setRol(Rol.CHOFER);
		chofer = usuarios.save(chofer);

		viajeEnCursoId = crearViaje(EstadoViaje.EN_CURSO).getId();
	}

	@Test
	void registraUnaPosicionEnUnViajeEnCurso() throws Exception {
		mockMvc.perform(post("/api/viajes/" + viajeEnCursoId + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content(posicion(-32.89, -68.84, T0)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").isNumber())
			.andExpect(jsonPath("$[0].latitud").value(-32.89))
			.andExpect(jsonPath("$[0].longitud").value(-68.84))
			.andExpect(jsonPath("$[0].registradoEn").value("2026-09-25T12:00:00Z"))
			.andExpect(jsonPath("$[0].recibidoEn").isNotEmpty());
	}

	@Test
	void aceptaUnLoteDePosicionesAcumuladasSinSenial() throws Exception {
		mockMvc.perform(post("/api/viajes/" + viajeEnCursoId + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					[%s, %s, %s]
					""".formatted(
						posicion(-32.89, -68.84, T0),
						posicion(-32.80, -68.70, T0.plusSeconds(60)),
						posicion(-32.70, -68.60, T0.plusSeconds(120)))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	void siNoSeIndicaLaHoraSeUsaLaDelServidor() throws Exception {
		mockMvc.perform(post("/api/viajes/" + viajeEnCursoId + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"latitud": -32.89, "longitud": -68.84}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$[0].registradoEn").isNotEmpty());
	}

	@Test
	void noSePuedenRegistrarPosicionesEnUnViajeProgramado() throws Exception {
		Long programado = crearViaje(EstadoViaje.PROGRAMADO).getId();

		mockMvc.perform(post("/api/viajes/" + programado + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content(posicion(-32.89, -68.84, T0)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Conflicto"));
	}

	@Test
	void noSePuedenRegistrarPosicionesEnUnViajeFinalizado() throws Exception {
		Long finalizado = crearViaje(EstadoViaje.FINALIZADO).getId();

		mockMvc.perform(post("/api/viajes/" + finalizado + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content(posicion(-32.89, -68.84, T0)))
			.andExpect(status().isConflict());
	}

	@Test
	void unViajePausadoSigueAceptandoPosiciones() throws Exception {
		// Se pausa el viaje existente en lugar de crear otro: el índice único
		// parcial de la base no permite dos viajes en ruta para el mismo camión.
		Viaje viaje = viajes.findById(viajeEnCursoId).orElseThrow();
		viaje.setEstado(EstadoViaje.PAUSADO);
		viajes.saveAndFlush(viaje);

		mockMvc.perform(post("/api/viajes/" + viajeEnCursoId + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content(posicion(-32.89, -68.84, T0)))
			.andExpect(status().isCreated());
	}

	@Test
	void rechazaCoordenadasFueraDeRango() throws Exception {
		mockMvc.perform(post("/api/viajes/" + viajeEnCursoId + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content(posicion(-200, -68.84, T0)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Datos inválidos"));
	}

	@Test
	void devuelve404AlRegistrarEnUnViajeInexistente() throws Exception {
		mockMvc.perform(post("/api/viajes/999999/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content(posicion(-32.89, -68.84, T0)))
			.andExpect(status().isNotFound());
	}

	@Test
	void laUltimaPosicionEsLaMasReciente() throws Exception {
		registrarTresPosiciones();

		mockMvc.perform(get("/api/viajes/" + viajeEnCursoId + "/posiciones/ultima"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.latitud").value(-32.70))
			.andExpect(jsonPath("$.registradoEn").value("2026-09-25T12:02:00Z"));
	}

	@Test
	void devuelve404SiElViajeTodaviaNoTienePosiciones() throws Exception {
		mockMvc.perform(get("/api/viajes/" + viajeEnCursoId + "/posiciones/ultima"))
			.andExpect(status().isNotFound());
	}

	@Test
	void elRecorridoVieneEnOrdenCronologico() throws Exception {
		registrarTresPosiciones();

		mockMvc.perform(get("/api/viajes/" + viajeEnCursoId + "/recorrido"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(3))
			.andExpect(jsonPath("$[0].registradoEn").value("2026-09-25T12:00:00Z"))
			.andExpect(jsonPath("$[2].registradoEn").value("2026-09-25T12:02:00Z"));
	}

	@Test
	void elRecorridoSePuedeAcotarPorRangoDeTiempo() throws Exception {
		registrarTresPosiciones();

		mockMvc.perform(get("/api/viajes/" + viajeEnCursoId + "/recorrido")
				.param("desde", "2026-09-25T12:00:30Z"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));

		mockMvc.perform(get("/api/viajes/" + viajeEnCursoId + "/recorrido")
				.param("desde", "2026-09-25T12:00:30Z")
				.param("hasta", "2026-09-25T12:01:30Z"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void laPosicionDeLosViajesActivosTraeUnaEntradaPorViaje() throws Exception {
		registrarTresPosiciones();

		mockMvc.perform(get("/api/viajes/activos/posiciones"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].viajeId").value(viajeEnCursoId))
			.andExpect(jsonPath("$[0].patente").value("AB123CD"))
			.andExpect(jsonPath("$[0].estado").value("EN_CURSO"))
			.andExpect(jsonPath("$[0].posicion.latitud").value(-32.70));
	}

	@Test
	void losViajesQueNoEstanEnRutaNoAparecenEnLasPosicionesActivas() throws Exception {
		registrarTresPosiciones();

		Viaje viaje = viajes.findById(viajeEnCursoId).orElseThrow();
		viaje.setEstado(EstadoViaje.FINALIZADO);
		viajes.saveAndFlush(viaje);

		mockMvc.perform(get("/api/viajes/activos/posiciones"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	private void registrarTresPosiciones() throws Exception {
		mockMvc.perform(post("/api/viajes/" + viajeEnCursoId + "/posiciones")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					[%s, %s, %s]
					""".formatted(
						posicion(-32.89, -68.84, T0),
						posicion(-32.80, -68.70, T0.plusSeconds(60)),
						posicion(-32.70, -68.60, T0.plusSeconds(120)))))
			.andExpect(status().isCreated());
	}

	private String posicion(double latitud, double longitud, Instant momento) {
		return """
			{"latitud": %s, "longitud": %s, "velocidadKmh": 80.0, "rumboGrados": 45.0, "registradoEn": "%s"}
			""".formatted(latitud, longitud, momento);
	}

	private Viaje crearViaje(EstadoViaje estado) {
		Viaje viaje = new Viaje();
		viaje.setCamion(camion);
		viaje.setChofer(chofer);
		viaje.setOrigenNombre("Mendoza");
		viaje.setOrigenLat(-32.89);
		viaje.setOrigenLon(-68.84);
		viaje.setDestinoNombre("Córdoba");
		viaje.setDestinoLat(-31.42);
		viaje.setDestinoLon(-64.18);
		viaje.setEstado(estado);
		return viajes.save(viaje);
	}

}
