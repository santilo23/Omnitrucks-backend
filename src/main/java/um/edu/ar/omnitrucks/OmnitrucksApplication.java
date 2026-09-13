package um.edu.ar.omnitrucks;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OmnitrucksApplication {

	/*
	 * Toda la aplicación trabaja en UTC. Se fija en un bloque estático para que
	 * aplique antes de abrir cualquier conexión, también cuando la clase la carga
	 * un test y no se ejecuta main().
	 *
	 * Además evita un error real: en Windows con zona Argentina, Java informa
	 * "America/Buenos_Aires", un alias viejo que el driver le envía a PostgreSQL
	 * al conectarse y que las versiones nuevas rechazan.
	 */
	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(OmnitrucksApplication.class, args);
	}

}
