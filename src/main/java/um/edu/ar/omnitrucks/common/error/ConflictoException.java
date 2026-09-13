package um.edu.ar.omnitrucks.common.error;

/**
 * La operación choca con el estado actual de los datos o con una regla de
 * negocio: una patente duplicada, una transición de estado inválida, un camión
 * que ya tiene un viaje en curso. Se traduce a una respuesta HTTP 409.
 */
public class ConflictoException extends RuntimeException {

	public ConflictoException(String mensaje) {
		super(mensaje);
	}

}
