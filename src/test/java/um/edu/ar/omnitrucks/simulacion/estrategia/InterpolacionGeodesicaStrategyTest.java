package um.edu.ar.omnitrucks.simulacion.estrategia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import um.edu.ar.omnitrucks.common.geo.Coordenada;

class InterpolacionGeodesicaStrategyTest {

	private final EstrategiaMovimiento estrategia = new InterpolacionGeodesicaStrategy();

	// Origen: CABA (-34.6037, -58.3816)
	// Destino: Rosario (-32.9468, -60.6393) - Distancia: ~278.500 metros
	private final Coordenada origen = new Coordenada(-34.6037, -58.3816);
	private final Coordenada destino = new Coordenada(-32.9468, -60.6393);

	@Test
	@DisplayName("Avanza un paso hacia el destino reduciendo la distancia restante")
	void avanzaHaciaElDestino() {
		double distanciaInicial = origen.distanciaMetros(destino);
		double pasoMetros = 1000.0; // 1 km

		Coordenada siguiente = estrategia.siguientePosicion(origen, destino, pasoMetros);

		double distanciaNueva = siguiente.distanciaMetros(destino);
		assertThat(distanciaNueva).isCloseTo(distanciaInicial - pasoMetros, within(10.0));
		assertThat(origen.distanciaMetros(siguiente)).isCloseTo(pasoMetros, within(10.0));
	}

	@Test
	@DisplayName("Si el paso supera o iguala la distancia restante, llega exactamente al destino")
	void llegaAlDestinoSiElPasoEsMayorQueLaDistancia() {
		double distanciaInicial = origen.distanciaMetros(destino);
		double pasoExcesivo = distanciaInicial + 5000.0;

		Coordenada siguiente = estrategia.siguientePosicion(origen, destino, pasoExcesivo);

		assertThat(siguiente).isEqualTo(destino);
		assertThat(siguiente.distanciaMetros(destino)).isEqualTo(0.0);
	}

	@Test
	@DisplayName("Si ya está en el destino, permanece en el destino")
	void permaneceEnDestinoSiYaLlego() {
		Coordenada siguiente = estrategia.siguientePosicion(destino, destino, 100.0);

		assertThat(siguiente).isEqualTo(destino);
	}
}
