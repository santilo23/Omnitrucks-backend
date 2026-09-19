package um.edu.ar.omnitrucks.camion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

/**
 * Recorre la pila completa —controlador, servicio, JPA y PostgreSQL— contra la
 * base levantada con docker compose.
 *
 * Cada test corre dentro de una transacción que se revierte al terminar, así
 * que no se pisan entre sí ni ensucian la base.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CamionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CamionRepository repositorio;

	private static final String SCANIA = """
		{"patente": "AB123CD", "marca": "Scania", "modelo": "R450", "anio": 2020, "capacidadKg": 30000}
		""";

	@BeforeEach
	void limpiar() {
		repositorio.deleteAll();
	}

	@Test
	void creaUnCamionYLoDevuelveConSuUbicacion() throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content(SCANIA))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.patente").value("AB123CD"))
			.andExpect(jsonPath("$.marca").value("Scania"))
			.andExpect(jsonPath("$.activo").value(true))
			.andExpect(jsonPath("$.createdAt").isNotEmpty());
	}

	@Test
	void normalizaLaPatenteAMayusculas() throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content("""
				{"patente": " ab123cd ", "marca": "Scania", "modelo": "R450"}
				"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.patente").value("AB123CD"));
	}

	@Test
	void rechazaUnaPatenteDuplicadaCon409() throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content(SCANIA))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content(SCANIA))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Conflicto"))
			.andExpect(jsonPath("$.detail").value("Ya existe un camión con la patente AB123CD."));
	}

	@Test
	void rechazaUnaPatenteConFormatoInvalido() throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content("""
				{"patente": "HOLA", "marca": "Scania", "modelo": "R450"}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Datos inválidos"))
			.andExpect(jsonPath("$.errores.patente").value("La patente debe tener formato ABC123 o AB123CD."));
	}

	@Test
	void rechazaCamposObligatoriosVacios() throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content("""
				{"patente": "", "marca": "", "modelo": ""}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errores.marca").value("La marca es obligatoria."))
			.andExpect(jsonPath("$.errores.modelo").value("El modelo es obligatorio."));
	}

	@Test
	void devuelve404AlPedirUnCamionInexistente() throws Exception {
		mockMvc.perform(get("/api/camiones/999999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Recurso no encontrado"));
	}

	@Test
	void actualizaUnCamionExistente() throws Exception {
		Long id = crearScania();

		mockMvc.perform(put("/api/camiones/" + id).contentType(MediaType.APPLICATION_JSON).content("""
				{"patente": "AB123CD", "marca": "Volvo", "modelo": "FH16", "anio": 2022, "capacidadKg": 35000}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.marca").value("Volvo"))
			.andExpect(jsonPath("$.capacidadKg").value(35000));
	}

	@Test
	void laBajaEsLogicaYSacaAlCamionDelListado() throws Exception {
		Long id = crearScania();

		mockMvc.perform(delete("/api/camiones/" + id)).andExpect(status().isNoContent());

		// Ya no aparece en el listado por defecto...
		mockMvc.perform(get("/api/camiones"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));

		// ...pero el registro sigue existiendo, marcado como inactivo.
		mockMvc.perform(get("/api/camiones").param("incluirInactivos", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].activo").value(false));

		assertThat(repositorio.findById(id)).isPresent();
	}

	@Test
	void noPermiteDarDeBajaDosVeces() throws Exception {
		Long id = crearScania();

		mockMvc.perform(delete("/api/camiones/" + id)).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/camiones/" + id)).andExpect(status().isConflict());
	}

	@Test
	void listaLosCamionesOrdenadosPorPatente() throws Exception {
		crear("ZZ999ZZ");
		crear("AA111AA");

		mockMvc.perform(get("/api/camiones"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].patente").value("AA111AA"))
			.andExpect(jsonPath("$[1].patente").value("ZZ999ZZ"));
	}

	private Long crearScania() throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content(SCANIA))
			.andExpect(status().isCreated());
		return repositorio.findByPatente("AB123CD").orElseThrow().getId();
	}

	private void crear(String patente) throws Exception {
		mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON).content("""
				{"patente": "%s", "marca": "Scania", "modelo": "R450"}
				""".formatted(patente)))
			.andExpect(status().isCreated());
	}

}
