package um.edu.ar.omnitrucks.common.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoordenadaTest {

	private static final double TOLERANCIA_METROS = 50.0;
	private static final double TOLERANCIA_GRADOS = 1.0;

	// Coordenadas reales de referencia
	// Obelisco, CABA: -34.6037, -58.3816
	// Monumento a la Bandera, Rosario: -32.9468, -60.6393
	// Distancia geodésica real en línea recta: ~278 km (278.000 m)
	private final Coordenada caba = new Coordenada(-34.6037, -58.3816);
	private final Coordenada rosario = new Coordenada(-32.9468, -60.6393);

	@Test
	@DisplayName("Valida los límites de latitud y longitud al instanciar")
	void validaLimitesGeograficos() {
		assertThatThrownBy(() -> new Coordenada(-91.0, 0.0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("latitud");

		assertThatThrownBy(() -> new Coordenada(91.0, 0.0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("latitud");

		assertThatThrownBy(() -> new Coordenada(0.0, -181.0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("longitud");

		assertThatThrownBy(() -> new Coordenada(0.0, 181.0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("longitud");
	}

	@Test
	@DisplayName("Calcula la distancia geodésica correcta entre dos puntos (Haversine)")
	void calculaDistanciaHaversine() {
		double distancia = caba.distanciaMetros(rosario);

		// Distancia esperada aproximada: 278.500 metros
		assertThat(distancia).isCloseTo(278_500.0, within(1000.0));

		// Distancia consigo mismo es 0
		assertThat(caba.distanciaMetros(caba)).isEqualTo(0.0);
	}

	@Test
	@DisplayName("Calcula el rumbo inicial (bearing) entre dos puntos en grados [0, 360)")
	void calculaRumboInicial() {
		double rumbo = caba.rumboHacia(rosario);

		// Desde CABA hacia Rosario es dirección Noroeste (~310° a 320°)
		assertThat(rumbo).isBetween(310.0, 325.0);
	}

	@Test
	@DisplayName("Desplaza una coordenada una distancia sobre un rumbo dado")
	void desplazaHaciaDestino() {
		double rumbo = caba.rumboHacia(rosario);
		double distanciaTotal = caba.distanciaMetros(rosario);

		// Si nos desplazamos 10 km hacia Rosario
		Coordenada puntoIntermedio = caba.desplazar(10_000.0, rumbo);

		// El punto intermedio debe estar a 10 km de CABA
		assertThat(caba.distanciaMetros(puntoIntermedio)).isCloseTo(10_000.0, within(TOLERANCIA_METROS));

		// Y la distancia restante hacia Rosario debe ser (total - 10 km)
		double distanciaRestante = puntoIntermedio.distanciaMetros(rosario);
		assertThat(distanciaRestante).isCloseTo(distanciaTotal - 10_000.0, within(TOLERANCIA_METROS));
	}

	@Test
	@DisplayName("Desplazarse la distancia total sobre el rumbo alcanza el destino")
	void desplazarDistanciaTotalAlcanzaDestino() {
		double rumbo = caba.rumboHacia(rosario);
		double distanciaTotal = caba.distanciaMetros(rosario);

		Coordenada llegada = caba.desplazar(distanciaTotal, rumbo);

		assertThat(llegada.latitud()).isCloseTo(rosario.latitud(), within(0.001));
		assertThat(llegada.longitud()).isCloseTo(rosario.longitud(), within(0.001));
	}
}
