package um.edu.ar.omnitrucks.simulacion.estrategia;

import um.edu.ar.omnitrucks.common.geo.Coordenada;

/**
 * Estrategia (Pattern: Strategy) para calcular el desplazamiento de un vehículo en simulación.
 */
public interface EstrategiaMovimiento {

	/**
	 * Calcula la siguiente coordenada a partir de la posición actual hacia el destino,
	 * avanzando una distancia determinada (paso en metros).
	 *
	 * @param actual Posición actual del camión
	 * @param destino Coordenada de destino final del viaje
	 * @param pasoMetros Distancia a avanzar en este ciclo de simulación
	 * @return Nueva coordenada alcanzada
	 */
	Coordenada siguientePosicion(Coordenada actual, Coordenada destino, double pasoMetros);
}
