package um.edu.ar.omnitrucks.common.geo;

import java.util.Objects;

/**
 * Value Object inmutable que representa una coordenada geográfica en la Tierra.
 *
 * Encapsula cálculos de geodesia (fórmulas de Haversine, cálculo de rumbo y
 * desplazamiento sobre la superficie esférica terrestre).
 */
public record Coordenada(double latitud, double longitud) {

	private static final double RADIO_TIERRA_METROS = 6_371_000.0;

	public Coordenada {
		if (latitud < -90.0 || latitud > 90.0) {
			throw new IllegalArgumentException("La latitud debe estar entre -90 y 90 grados: " + latitud);
		}
		if (longitud < -180.0 || longitud > 180.0) {
			throw new IllegalArgumentException("La longitud debe estar entre -180 y 180 grados: " + longitud);
		}
	}

	/**
	 * Calcula la distancia en línea recta sobre la superficie terrestre (fórmula del Haversine).
	 *
	 * @param destino Punto de llegada
	 * @return Distancia en metros
	 */
	public double distanciaMetros(Coordenada destino) {
		Objects.requireNonNull(destino, "La coordenada de destino no puede ser nula.");

		if (this.equals(destino)) {
			return 0.0;
		}

		double phi1 = Math.toRadians(this.latitud);
		double phi2 = Math.toRadians(destino.latitud);
		double deltaPhi = Math.toRadians(destino.latitud - this.latitud);
		double deltaLambda = Math.toRadians(destino.longitud - this.longitud);

		double a = Math.sin(deltaPhi / 2.0) * Math.sin(deltaPhi / 2.0)
				+ Math.cos(phi1) * Math.cos(phi2)
				* Math.sin(deltaLambda / 2.0) * Math.sin(deltaLambda / 2.0);

		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		return RADIO_TIERRA_METROS * c;
	}

	/**
	 * Calcula el rumbo inicial (azimuth / bearing) en grados respecto al norte geográfico [0, 360).
	 *
	 * @param destino Punto hacia el que se apunta
	 * @return Rumbo en grados sexagesimales (0 = Norte, 90 = Este, 180 = Sur, 270 = Oeste)
	 */
	public double rumboHacia(Coordenada destino) {
		Objects.requireNonNull(destino, "La coordenada de destino no puede ser nula.");

		double phi1 = Math.toRadians(this.latitud);
		double phi2 = Math.toRadians(destino.latitud);
		double deltaLambda = Math.toRadians(destino.longitud - this.longitud);

		double y = Math.sin(deltaLambda) * Math.cos(phi2);
		double x = Math.cos(phi1) * Math.sin(phi2)
				- Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda);

		double rumboRadianes = Math.atan2(y, x);
		double rumboGrados = (Math.toDegrees(rumboRadianes) + 360.0) % 360.0;

		return rumboGrados;
	}

	/**
	 * Desplaza la coordenada actual una distancia dada siguiendo un rumbo constante.
	 *
	 * @param distanciaMetros Distancia a avanzar en metros
	 * @param rumboGrados Rumbo en grados [0, 360)
	 * @return Nueva coordenada calculada
	 */
	public Coordenada desplazar(double distanciaMetros, double rumboGrados) {
		if (distanciaMetros <= 0.0) {
			return this;
		}

		double delta = distanciaMetros / RADIO_TIERRA_METROS;
		double theta = Math.toRadians(rumboGrados);
		double phi1 = Math.toRadians(this.latitud);
		double lambda1 = Math.toRadians(this.longitud);

		double phi2 = Math.asin(
				Math.sin(phi1) * Math.cos(delta)
				+ Math.cos(phi1) * Math.sin(delta) * Math.cos(theta)
		);

		double lambda2 = lambda1 + Math.atan2(
				Math.sin(theta) * Math.sin(delta) * Math.cos(phi1),
				Math.cos(delta) - Math.sin(phi1) * Math.sin(phi2)
		);

		// Normalizar longitud a [-180, 180]
		lambda2 = (lambda2 + 3.0 * Math.PI) % (2.0 * Math.PI) - Math.PI;

		return new Coordenada(Math.toDegrees(phi2), Math.toDegrees(lambda2));
	}
}
