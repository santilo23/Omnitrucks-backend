package um.edu.ar.omnitrucks.camion.domain.factory;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import um.edu.ar.omnitrucks.camion.Camion;

/**
 * Patrón Factory para encapsular la instanciación y validación invariante
 * de entidades de la flota.
 */
@Component
public class CamionFactory {

	private static final Pattern PATENTE_RE = Pattern.compile(
			"^(?:[A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2})$"
	);

	/**
	 * Crea una nueva instancia de Camion asegurando que la patente cumpla con los
	 * formatos oficiales argentinos (ABC123 o AB123CD) y que los datos estén normalizados.
	 */
	public Camion crear(String patenteRaw, String marca, String modelo, Integer anio, Integer capacidadKg) {
		if (patenteRaw == null) {
			throw new IllegalArgumentException("La patente no puede ser nula.");
		}

		String patente = patenteRaw.trim().toUpperCase();
		if (!PATENTE_RE.matcher(patente).matches()) {
			throw new IllegalArgumentException("El formato de la patente no es válido: " + patenteRaw);
		}

		if (marca == null || marca.trim().isEmpty()) {
			throw new IllegalArgumentException("La marca no puede estar vacía.");
		}

		if (modelo == null || modelo.trim().isEmpty()) {
			throw new IllegalArgumentException("El modelo no puede estar vacío.");
		}

		Camion camion = new Camion();
		camion.setPatente(patente);
		camion.setMarca(marca.trim());
		camion.setModelo(modelo.trim());
		camion.setAnio(anio);
		camion.setCapacidadKg(capacidadKg);
		camion.setActivo(true);

		return camion;
	}

}
