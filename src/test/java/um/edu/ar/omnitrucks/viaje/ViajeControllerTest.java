package um.edu.ar.omnitrucks.viaje;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

/**
 * Comportamiento esperado de los viajes y de su máquina de estados.
 *
 *   PROGRAMADO ──> EN_CURSO ──> FINALIZADO
 *                     ↕
 *                  PAUSADO
 *   (cualquiera salvo FINALIZADO) ──> CANCELADO
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ViajeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CamionRepository camiones;

	@Autowired
	private UsuarioRepository usuarios;

	private Long camionId;
	private Long choferId;

	@Autowired
	private ViajeRepository viajes;

	@BeforeEach
	void prepararDatos() {
		// El test no asume que la base esté vacía: puede haber quedado algo de
		// una prueba manual. Se limpia respetando el orden de las claves foráneas
		// y, como todo corre en una transacción, se revierte al terminar.
		//
		// Se usa deleteAllInBatch y no deleteAll porque JPA ordena los INSERT
		// antes que los DELETE al sincronizar: el alta de abajo se ejecutaría
		// antes del borrado y chocaría con la patente ya existente.
		viajes.deleteAllInBatch();
		camiones.deleteAllInBatch();
		usuarios.deleteAllInBatch();

		Camion camion = new Camion();
		camion.setPatente("AB123CD");
		camion.setMarca("Scania");
		camion.setModelo("R450");
		camionId = camiones.save(camion).getId();

		Usuario chofer = new Usuario();
		chofer.setEmail("chofer@omnitrucks.com");
		chofer.setPasswordHash("no-importa-todavia");
		chofer.setNombre("Juan");
		chofer.setApellido("Pérez");
		chofer.setRol(Rol.CHOFER);
		choferId = usuarios.save(chofer).getId();
	}

	@Test
	void unViajeNuevoNaceProgramadoYSinSalidaReal() throws Exception {
		mockMvc.perform(post("/api/viajes").contentType(MediaType.APPLICATION_JSON).content(nuevoViaje()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.estado").value("PROGRAMADO"))
			.andExpect(jsonPath("$.salidaReal").doesNotExist())
			.andExpect(jsonPath("$.camion.patente").value("AB123CD"))
			.andExpect(jsonPath("$.chofer.nombreCompleto").value("Juan Pérez"));
	}

	@Test
	void iniciarUnViajeLoPoneEnCursoYRegistraLaSalida() throws Exception {
		Long id = crearViaje();

		mockMvc.perform(post("/api/viajes/" + id + "/iniciar"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("EN_CURSO"))
			.andExpect(jsonPath("$.salidaReal").isNotEmpty());
	}

	@Test
	void unViajeProgramadoNoSePuedeFinalizar() throws Exception {
		Long id = crearViaje();

		mockMvc.perform(post("/api/viajes/" + id + "/finalizar"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Conflicto"));
	}

	@Test
	void unViajeNoSePuedeIniciarDosVeces() throws Exception {
		Long id = crearViaje();

		mockMvc.perform(post("/api/viajes/" + id + "/iniciar")).andExpect(status().isOk());
		mockMvc.perform(post("/api/viajes/" + id + "/iniciar")).andExpect(status().isConflict());
	}

	@Test
	void unViajeEnCursoSePausaYSeReanuda() throws Exception {
		Long id = crearViaje();
		mockMvc.perform(post("/api/viajes/" + id + "/iniciar")).andExpect(status().isOk());

		mockMvc.perform(post("/api/viajes/" + id + "/pausar"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("PAUSADO"));

		mockMvc.perform(post("/api/viajes/" + id + "/reanudar"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("EN_CURSO"));
	}

	@Test
	void finalizarUnViajeEnCursoRegistraLaLlegada() throws Exception {
		Long id = crearViaje();
		mockMvc.perform(post("/api/viajes/" + id + "/iniciar")).andExpect(status().isOk());

		mockMvc.perform(post("/api/viajes/" + id + "/finalizar"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("FINALIZADO"))
			.andExpect(jsonPath("$.llegadaReal").isNotEmpty());
	}

	@Test
	void unViajeFinalizadoNoSePuedeCancelar() throws Exception {
		Long id = crearViaje();
		mockMvc.perform(post("/api/viajes/" + id + "/iniciar")).andExpect(status().isOk());
		mockMvc.perform(post("/api/viajes/" + id + "/finalizar")).andExpect(status().isOk());

		mockMvc.perform(post("/api/viajes/" + id + "/cancelar"))
			.andExpect(status().isConflict());
	}

	@Test
	void unCamionNoPuedeEstarEnDosViajesALaVez() throws Exception {
		Long primero = crearViaje();
		mockMvc.perform(post("/api/viajes/" + primero + "/iniciar")).andExpect(status().isOk());

		Long segundo = crearViaje();
		mockMvc.perform(post("/api/viajes/" + segundo + "/iniciar"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("El camión AB123CD ya tiene un viaje en curso."));
	}

	@Test
	void noSePuedeCrearUnViajeConUnCamionInexistente() throws Exception {
		mockMvc.perform(post("/api/viajes").contentType(MediaType.APPLICATION_JSON).content("""
				{"camionId": 999999, "choferId": %d,
				 "origenNombre": "Mendoza", "origenLat": -32.89, "origenLon": -68.84,
				 "destinoNombre": "Córdoba", "destinoLat": -31.42, "destinoLon": -64.18}
				""".formatted(choferId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Recurso no encontrado"));
	}

	@Test
	void rechazaCoordenadasFueraDeRango() throws Exception {
		mockMvc.perform(post("/api/viajes").contentType(MediaType.APPLICATION_JSON).content("""
				{"camionId": %d, "choferId": %d,
				 "origenNombre": "Mendoza", "origenLat": -200, "origenLon": -68.84,
				 "destinoNombre": "Córdoba", "destinoLat": -31.42, "destinoLon": -64.18}
				""".formatted(camionId, choferId)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errores.origenLat").isNotEmpty());
	}

	@Test
	void soloSeListanComoActivosLosViajesEnCursoOPausados() throws Exception {
		Long programado = crearViaje();

		mockMvc.perform(get("/api/viajes/activos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(post("/api/viajes/" + programado + "/iniciar")).andExpect(status().isOk());

		mockMvc.perform(get("/api/viajes/activos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].estado").value("EN_CURSO"));
	}

	@Test
	void sePuedeFiltrarElListadoPorEstado() throws Exception {
		crearViaje();

		mockMvc.perform(get("/api/viajes").param("estado", "PROGRAMADO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/api/viajes").param("estado", "FINALIZADO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	private String nuevoViaje() {
		return """
			{"camionId": %d, "choferId": %d,
			 "origenNombre": "Mendoza", "origenLat": -32.89, "origenLon": -68.84,
			 "destinoNombre": "Córdoba", "destinoLat": -31.42, "destinoLon": -64.18,
			 "descripcionCarga": "Vino fraccionado"}
			""".formatted(camionId, choferId);
	}

	private Long crearViaje() throws Exception {
		String respuesta = mockMvc.perform(
				post("/api/viajes").contentType(MediaType.APPLICATION_JSON).content(nuevoViaje()))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();

		return com.jayway.jsonpath.JsonPath.parse(respuesta).read("$.id", Integer.class).longValue();
	}

}
