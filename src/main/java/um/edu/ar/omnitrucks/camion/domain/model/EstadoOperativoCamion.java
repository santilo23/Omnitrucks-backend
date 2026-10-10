package um.edu.ar.omnitrucks.camion.domain.model;

/**
 * Estado operativo de un camión dentro de la gestión de flota.
 */
public enum EstadoOperativoCamion {

	/** Camión activo listo para ser asignado a un viaje. */
	DISPONIBLE,

	/** Camión actualmente en ruta (con viaje EN_CURSO o PAUSADO). */
	EN_TRANSITO,

	/** Camión fuera de servicio o dado de baja lógica. */
	INACTIVO

}
