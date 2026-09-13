package um.edu.ar.omnitrucks.common.error;

/**
 * El recurso pedido no existe. Se traduce a una respuesta HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

	public RecursoNoEncontradoException(String mensaje) {
		super(mensaje);
	}

	public RecursoNoEncontradoException(String recurso, Object id) {
		super("No existe %s con id %s.".formatted(recurso, id));
	}

}
