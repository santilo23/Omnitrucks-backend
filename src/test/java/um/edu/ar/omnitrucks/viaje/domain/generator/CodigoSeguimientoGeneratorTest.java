package um.edu.ar.omnitrucks.viaje.domain.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodigoSeguimientoGeneratorTest {

	@Test
	@DisplayName("generar genera un código con formato TRK- y longitud esperada")
	void generarGeneraCodigoConFormatoCorrecto() {
		String codigo = CodigoSeguimientoGenerator.generar();

		assertThat(codigo).isNotNull();
		assertThat(codigo).startsWith("TRK-");
		assertThat(codigo).hasSize(12);
		assertThat(CodigoSeguimientoGenerator.esValido(codigo)).isTrue();
	}

	@Test
	@DisplayName("generar produce códigos unívocos sucesivos sin colisiones inmediatas")
	void generarProduceCodigosUnicos() {
		Set<String> generados = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			String codigo = CodigoSeguimientoGenerator.generar();
			boolean agregado = generados.add(codigo);
			assertThat(agregado).as("El código %s no debería repetirse", codigo).isTrue();
		}
	}

	@Test
	@DisplayName("esValido verifica correctamente distintos formatos de códigos")
	void esValidoVerificaFormatos() {
		assertThat(CodigoSeguimientoGenerator.esValido("TRK-ABC12345")).isTrue();
		assertThat(CodigoSeguimientoGenerator.esValido("trk-abc12345")).isTrue();
		assertThat(CodigoSeguimientoGenerator.esValido(null)).isFalse();
		assertThat(CodigoSeguimientoGenerator.esValido("")).isFalse();
		assertThat(CodigoSeguimientoGenerator.esValido("ABC12345")).isFalse();
		assertThat(CodigoSeguimientoGenerator.esValido("TRK-")).isFalse();
	}

}
