package um.edu.ar.omnitrucks.viaje.domain.generator;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Generador y validador de códigos únicos de seguimiento para envíos.
 * Formato estándar: TRK-XXXXXXXX (Prefijo TRK seguido de caracteres alfanuméricos en mayúsculas).
 */
public final class CodigoSeguimientoGenerator {

	private static final String PREFIJO = "TRK-";
	private static final String CARACTERES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // Alfanumérico sin caracteres ambiguos (0/O, 1/I)
	private static final int LONGITUD_ALEATORIA = 8;
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Pattern PATRON_VALIDO = Pattern.compile("^TRK-[A-Z0-9]{4,16}$");

	private CodigoSeguimientoGenerator() {
	}

	/**
	 * Genera un código de seguimiento pseudoaleatorio criptográficamente seguro.
	 */
	public static String generar() {
		StringBuilder sb = new StringBuilder(PREFIJO);
		for (int i = 0; i < LONGITUD_ALEATORIA; i++) {
			int index = RANDOM.nextInt(CARACTERES.length());
			sb.append(CARACTERES.charAt(index));
		}
		return sb.toString();
	}

	/**
	 * Valida si un texto cumple con el formato aceptado para códigos de seguimiento.
	 */
	public static boolean esValido(String codigo) {
		if (codigo == null) {
			return false;
		}
		return PATRON_VALIDO.matcher(codigo.trim().toUpperCase()).matches();
	}

}
