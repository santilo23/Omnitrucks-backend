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
	}

	public static void main(String[] args) {
		SpringApplication.run(OmnitrucksApplication.class, args);
	}

}
