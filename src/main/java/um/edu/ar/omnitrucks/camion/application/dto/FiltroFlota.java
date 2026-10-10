package um.edu.ar.omnitrucks.camion.application.dto;

/**
 * Criterio de filtrado para la flota en el panel de administración.
 */
public enum FiltroFlota {

	/** Todos los camiones registrados (histórico completo). */
	TODOS,

	/** Únicamente camiones habilitados operativamente. */
	ACTIVOS,

	/** Camiones dados de baja lógica o fuera de servicio. */
	INACTIVOS

}
