package um.edu.ar.omnitrucks.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones de la aplicación a respuestas {@link ProblemDetail}
 * (RFC 9457), para que todos los errores de la API tengan el mismo formato.
 *
 * Al extender {@link ResponseEntityExceptionHandler}, los errores estándar de
 * Spring MVC (JSON mal formado, método no permitido, etc.) también salen como
 * ProblemDetail.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RecursoNoEncontradoException.class)
	ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problema.setTitle("Recurso no encontrado");
		return problema;
	}

	@ExceptionHandler(ConflictoException.class)
	ProblemDetail manejarConflicto(ConflictoException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problema.setTitle("Conflicto");
		return problema;
	}

	/**
	 * Errores de Bean Validation en el cuerpo de la request. Además del mensaje
	 * general, agrega un mapa {@code errores} campo → mensaje para que la app
	 * pueda mostrar cada error debajo de su campo.
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errores = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
			.forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

		ProblemDetail problema = ex.getBody();
		problema.setTitle("Datos inválidos");
		problema.setDetail("La solicitud tiene campos con errores.");
		problema.setProperty("errores", errores);

		return handleExceptionInternal(ex, problema, headers, status, request);
	}

	/**
	 * Errores de validación cuando el cuerpo es una lista o el dato viene en un
	 * parámetro. Spring lanza acá una excepción distinta a la de un objeto
	 * suelto, y sin esto el error saldría con el formato genérico en lugar del
	 * del proyecto. Lo usa el alta de posiciones, que recibe un arreglo.
	 */
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
			HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errores = new LinkedHashMap<>();

		for (ParameterValidationResult resultado : ex.getParameterValidationResults()) {
			for (MessageSourceResolvable error : resultado.getResolvableErrors()) {
				String campo = (error instanceof FieldError falla)
					? falla.getField()
					: resultado.getMethodParameter().getParameterName();
				errores.putIfAbsent(campo, error.getDefaultMessage());
			}
		}

		ProblemDetail problema = ProblemDetail.forStatus(status);
		problema.setTitle("Datos inválidos");
		problema.setDetail("La solicitud tiene campos con errores.");
		problema.setProperty("errores", errores);

		return handleExceptionInternal(ex, problema, headers, status, request);
	}

}
