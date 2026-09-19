package um.edu.ar.omnitrucks.common.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Se limita a un controlador de prueba: sin el atributo `controllers`, la
// prueba levantaría todos los controladores reales de la aplicación y fallaría
// por los servicios que necesitan.
@WebMvcTest(controllers = GlobalExceptionHandlerTest.ControladorDePrueba.class)
@Import(GlobalExceptionHandlerTest.ControladorDePrueba.class)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void recursoNoEncontradoDevuelve404ComoProblemDetail() throws Exception {
		mockMvc.perform(get("/prueba/no-encontrado"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.title").value("Recurso no encontrado"))
			.andExpect(jsonPath("$.detail").value("No existe camión con id 42."))
			.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void conflictoDevuelve409ComoProblemDetail() throws Exception {
		mockMvc.perform(get("/prueba/conflicto"))
			.andExpect(status().isConflict())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.title").value("Conflicto"))
			.andExpect(jsonPath("$.detail").value("Ya existe un camión con esa patente."));
	}

	@Test
	void validacionDevuelve400ConErroresPorCampo() throws Exception {
		mockMvc.perform(post("/prueba/validacion")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"patente\": \"\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.title").value("Datos inválidos"))
			.andExpect(jsonPath("$.errores.patente").value("La patente es obligatoria."));
	}

	@Test
	void jsonMalFormadoTambienSaleComoProblemDetail() throws Exception {
		mockMvc.perform(post("/prueba/validacion")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{esto no es json"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
	}

	record PedidoDePrueba(@NotBlank(message = "La patente es obligatoria.") String patente) {
	}

	@RestController
	static class ControladorDePrueba {

		@GetMapping("/prueba/no-encontrado")
		void noEncontrado() {
			throw new RecursoNoEncontradoException("camión", 42);
		}

		@GetMapping("/prueba/conflicto")
		void conflicto() {
			throw new ConflictoException("Ya existe un camión con esa patente.");
		}

		@PostMapping("/prueba/validacion")
		void validacion(@Valid @RequestBody PedidoDePrueba pedido) {
		}

	}

}
