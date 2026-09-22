package um.edu.ar.omnitrucks.viaje;

/**
 * Estados por los que pasa un viaje.
 *
 * <pre>
 *   PROGRAMADO ──> EN_CURSO ──> FINALIZADO
 *                     ↕
 *                  PAUSADO
 *
 *   Cualquiera salvo FINALIZADO ──> CANCELADO
 * </pre>
 */
public enum EstadoViaje {

	PROGRAMADO,
	EN_CURSO,
	PAUSADO,
	FINALIZADO,
	CANCELADO;

	/** Un viaje está en ruta mientras el camión no llegó ni se canceló. */
	public boolean enRuta() {
		return this == EN_CURSO || this == PAUSADO;
	}

	/** Estados desde los que ya no se puede hacer ninguna transición. */
	public boolean esFinal() {
		return this == FINALIZADO || this == CANCELADO;
	}

}
