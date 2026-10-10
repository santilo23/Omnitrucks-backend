package um.edu.ar.omnitrucks.camion.domain.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.domain.model.EstadoOperativoCamion;
import um.edu.ar.omnitrucks.camion.domain.port.VerificadorViajesActivosPort;

@ExtendWith(MockitoExtension.class)
class DeterminadorEstadoCamionStrategyTest {

	@Mock
	private VerificadorViajesActivosPort verificadorViajesActivos;

	private EvaluadorEstadoCamionStrategyImpl strategy;

	private Camion camion;

	@BeforeEach
	void setUp() {
		strategy = new EvaluadorEstadoCamionStrategyImpl(verificadorViajesActivos);

		camion = new Camion();
		camion.setId(10L);
		camion.setPatente("AB123CD");
		camion.setMarca("Scania");
		camion.setModelo("R450");
		camion.setActivo(true);
	}

	@Test
	@DisplayName("Retorna INACTIVO si el camión tiene activo = false")
	void retornaInactivoSiNoEstaActivo() {
		camion.setActivo(false);

		EstadoOperativoCamion estado = strategy.determinarEstado(camion);

		assertThat(estado).isEqualTo(EstadoOperativoCamion.INACTIVO);
	}

	@Test
	@DisplayName("Retorna EN_TRANSITO si el camión está activo y tiene un viaje en curso o pausado")
	void retornaEnTransitoSiTieneViajeActivo() {
		when(verificadorViajesActivos.camionTieneViajeActivo(10L)).thenReturn(true);

		EstadoOperativoCamion estado = strategy.determinarEstado(camion);

		assertThat(estado).isEqualTo(EstadoOperativoCamion.EN_TRANSITO);
	}

	@Test
	@DisplayName("Retorna DISPONIBLE si el camión está activo y no tiene ningún viaje activo")
	void retornaDisponibleSiNoTieneViajeActivo() {
		when(verificadorViajesActivos.camionTieneViajeActivo(10L)).thenReturn(false);

		EstadoOperativoCamion estado = strategy.determinarEstado(camion);

		assertThat(estado).isEqualTo(EstadoOperativoCamion.DISPONIBLE);
	}
}
