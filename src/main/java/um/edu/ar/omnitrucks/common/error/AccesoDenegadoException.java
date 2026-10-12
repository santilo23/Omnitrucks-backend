package um.edu.ar.omnitrucks.common.error;

/**
 * Excepción lanzada cuando un usuario autenticado intenta acceder a un recurso
 * o información que no le corresponde según las reglas de negocio (ej. consultar envíos de otro cliente).
 */
public class AccesoDenegadoException extends RuntimeException {

	public AccesoDenegadoException(String mensaje) {
		super(mensaje);
	}

}
