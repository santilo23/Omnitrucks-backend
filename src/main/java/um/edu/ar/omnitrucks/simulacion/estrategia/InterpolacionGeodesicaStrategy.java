package um.edu.ar.omnitrucks.simulacion.estrategia;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.common.geo.Coordenada;

/**
 * Estrategia de movimiento basada en interpolación directa geodésica.
 * Avanza sobre el gran círculo (ortodrómica) directo hacia el punto de destino.
 */
@Component
public class InterpolacionGeodesicaStrategy implements EstrategiaMovimiento {

	@Override
	public Coordenada siguientePosicion(Coordenada actual, Coordenada destino, double pasoMetros) {
		double distanciaRestante = actual.distanciaMetros(destino);

		// Si el paso alcanza o sobrepasa el destino, llegamos exactamente al destino
		if (distanciaRestante <= pasoMetros) {
			return destino;
		}

		double rumbo = actual.rumboHacia(destino);
		return actual.desplazar(pasoMetros, rumbo);
	}
}
