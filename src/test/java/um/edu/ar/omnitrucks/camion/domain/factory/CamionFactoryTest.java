package um.edu.ar.omnitrucks.camion.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import um.edu.ar.omnitrucks.camion.Camion;

class CamionFactoryTest {

	private final CamionFactory factory = new CamionFactory();

	@Test
	@DisplayName("Crea un camión normalizando la patente a mayúsculas y quitando espacios")
	void creaCamionNormalizandoPatente() {
		Camion camion = factory.crear("  ab123cd  ", " Scania ", " R450 ", 2022, 32000);

		assertThat(camion.getPatente()).isEqualTo("AB123CD");
		assertThat(camion.getMarca()).isEqualTo("Scania");
		assertThat(camion.getModelo()).isEqualTo("R450");
		assertThat(camion.getAnio()).isEqualTo(2022);
		assertThat(camion.getCapacidadKg()).isEqualTo(32000);
		assertThat(camion.isActivo()).isTrue();
	}

	@Test
	@DisplayName("Crea un camión con patente tradicional argentina (3 letras y 3 números)")
	void creaCamionPatenteTradicional() {
		Camion camion = factory.crear("abc123", "Mercedes-Benz", "Actros", 2018, 28000);

		assertThat(camion.getPatente()).isEqualTo("ABC123");
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   ", "INVALIDA", "123456", "A1B2C3" })
	@DisplayName("Rechaza patentes con formato no compatible con Argentina")
	void rechazaPatentesInvalidas(String patenteInvalida) {
		assertThatThrownBy(() -> factory.crear(patenteInvalida, "Volvo", "FH", 2020, 30000))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("patente");
	}

	@Test
	@DisplayName("Rechaza marca o modelo vacíos")
	void rechazaCamposObligatoriosVacios() {
		assertThatThrownBy(() -> factory.crear("AB123CD", "", "R450", 2022, 30000))
			.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> factory.crear("AB123CD", "Scania", "  ", 2022, 30000))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
