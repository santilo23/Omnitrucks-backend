package um.edu.ar.omnitrucks;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OmnitrucksApplication {

	/*
	 * Toda la aplicación trabaja en UTC.
	 *
	 * Ojo: esto NO alcanza por sí solo. El bloque estático corre recién cuando
	 * Java inicializa esta clase, y Spring puede abrir la conexión a la base
	 * antes de eso. La garantía real es arrancar la JVM con -Duser.timezone=UTC,
	 * que el pom.xml ya configura para `spring-boot:run` y para los tests.
	 * Esto queda como red de seguridad para cuando se ejecuta el jar empaquetado.
	 */
	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		cargarArchivoEnv();
	}

	/**
	 * Carga variables locales desde .env en System properties si el archivo existe.
	 * Permite inyectar contraseñas y credenciales de forma segura sin subirlas a Git.
	 */
	private static void cargarArchivoEnv() {
		java.nio.file.Path envPath = java.nio.file.Path.of(".env");
		if (java.nio.file.Files.exists(envPath)) {
			try {
				java.nio.file.Files.readAllLines(envPath).forEach(line -> {
					String trimmed = line.trim();
					if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
						int idx = trimmed.indexOf('=');
						String key = trimmed.substring(0, idx).trim();
						String val = trimmed.substring(idx + 1).trim();
						if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
							val = val.substring(1, val.length() - 1);
						}
						if (System.getProperty(key) == null && System.getenv(key) == null) {
							System.setProperty(key, val);
						}
					}
				});
			} catch (java.io.IOException ignored) {
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(OmnitrucksApplication.class, args);
	}

}
