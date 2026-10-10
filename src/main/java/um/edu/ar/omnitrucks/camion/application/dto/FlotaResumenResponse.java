package um.edu.ar.omnitrucks.camion.application.dto;

/**
 * Resumen cuantitativo del estado de la flota para el panel de administración.
 */
public record FlotaResumenResponse(
	long total,
	long activos,
	long inactivos,
	long disponibles,
	long enTransito
) {
}
