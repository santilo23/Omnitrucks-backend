package um.edu.ar.omnitrucks.camion.domain.port;

/**
 * Puerto de dominio que permite consultar si un camión tiene viajes activos
 * sin acoplar el dominio de Camion directamente a la persistencia de Viaje.
 */
public interface VerificadorViajesActivosPort {

	boolean camionTieneViajeActivo(Long camionId);

}
