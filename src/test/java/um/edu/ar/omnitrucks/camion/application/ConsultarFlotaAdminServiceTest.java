package um.edu.ar.omnitrucks.camion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.application.dto.FiltroFlota;
import um.edu.ar.omnitrucks.camion.application.dto.FlotaResumenResponse;
import um.edu.ar.omnitrucks.camion.application.service.ConsultarFlotaAdminService;
import um.edu.ar.omnitrucks.camion.domain.model.EstadoOperativoCamion;
import um.edu.ar.omnitrucks.camion.domain.port.CamionRepositoryPort;
import um.edu.ar.omnitrucks.camion.domain.strategy.DeterminadorEstadoCamionStrategy;
import um.edu.ar.omnitrucks.camion.dto.CamionResponse;

@ExtendWith(MockitoExtension.class)
class ConsultarFlotaAdminServiceTest {

	@Mock
	private CamionRepositoryPort camionRepository;

	@Mock
	private DeterminadorEstadoCamionStrategy strategy;

	private ConsultarFlotaAdminService service;

	private Camion camionActivo1;
	private Camion camionActivo2;
	private Camion camionInactivo;

	@BeforeEach
	void setUp() {
		service = new ConsultarFlotaAdminService(camionRepository, strategy);

		camionActivo1 = new Camion();
		camionActivo1.setId(1L);
		camionActivo1.setPatente("AA111AA");
		camionActivo1.setMarca("Scania");
		camionActivo1.setModelo("R450");
		camionActivo1.setActivo(true);

		camionActivo2 = new Camion();
		camionActivo2.setId(2L);
		camionActivo2.setPatente("BB222BB");
		camionActivo2.setMarca("Volvo");
		camionActivo2.setModelo("FH");
		camionActivo2.setActivo(true);

		camionInactivo = new Camion();
		camionInactivo.setId(3L);
		camionInactivo.setPatente("CC333CC");
		camionInactivo.setMarca("Mercedes-Benz");
		camionInactivo.setModelo("Actros");
		camionInactivo.setActivo(false);
	}

	@Test
	@DisplayName("Consulta flota con filtro ACTIVOS devolviendo solo camiones activos con sus estados operativos")
	void consultaFlotaActivos() {
		when(camionRepository.listarActivosOrdenadosPorPatente()).thenReturn(List.of(camionActivo1, camionActivo2));
		when(strategy.determinarEstado(camionActivo1)).thenReturn(EstadoOperativoCamion.DISPONIBLE);
		when(strategy.determinarEstado(camionActivo2)).thenReturn(EstadoOperativoCamion.EN_TRANSITO);

		List<CamionResponse> resultado = service.consultarFlota(FiltroFlota.ACTIVOS);

		assertThat(resultado).hasSize(2);
		assertThat(resultado.get(0).patente()).isEqualTo("AA111AA");
		assertThat(resultado.get(0).estadoOperativo()).isEqualTo(EstadoOperativoCamion.DISPONIBLE);
		assertThat(resultado.get(1).patente()).isEqualTo("BB222BB");
		assertThat(resultado.get(1).estadoOperativo()).isEqualTo(EstadoOperativoCamion.EN_TRANSITO);
	}

	@Test
	@DisplayName("Consulta flota con filtro INACTIVOS devolviendo solo camiones dados de baja")
	void consultaFlotaInactivos() {
		when(camionRepository.listarInactivosOrdenadosPorPatente()).thenReturn(List.of(camionInactivo));
		when(strategy.determinarEstado(camionInactivo)).thenReturn(EstadoOperativoCamion.INACTIVO);

		List<CamionResponse> resultado = service.consultarFlota(FiltroFlota.INACTIVOS);

		assertThat(resultado).hasSize(1);
		assertThat(resultado.get(0).patente()).isEqualTo("CC333CC");
		assertThat(resultado.get(0).activo()).isFalse();
		assertThat(resultado.get(0).estadoOperativo()).isEqualTo(EstadoOperativoCamion.INACTIVO);
	}

	@Test
	@DisplayName("Consulta flota con filtro TODOS devolviendo tanto activos como inactivos")
	void consultaFlotaTodos() {
		when(camionRepository.listarTodosOrdenadosPorPatente()).thenReturn(List.of(camionActivo1, camionActivo2, camionInactivo));
		when(strategy.determinarEstado(camionActivo1)).thenReturn(EstadoOperativoCamion.DISPONIBLE);
		when(strategy.determinarEstado(camionActivo2)).thenReturn(EstadoOperativoCamion.EN_TRANSITO);
		when(strategy.determinarEstado(camionInactivo)).thenReturn(EstadoOperativoCamion.INACTIVO);

		List<CamionResponse> resultado = service.consultarFlota(FiltroFlota.TODOS);

		assertThat(resultado).hasSize(3);
	}

	@Test
	@DisplayName("Calcula el resumen cuantitativo de la flota correctamente")
	void calculaResumenFlota() {
		when(camionRepository.contarTotal()).thenReturn(3L);
		when(camionRepository.contarActivos()).thenReturn(2L);
		when(camionRepository.contarInactivos()).thenReturn(1L);
		when(camionRepository.listarActivosOrdenadosPorPatente()).thenReturn(List.of(camionActivo1, camionActivo2));
		when(strategy.determinarEstado(camionActivo1)).thenReturn(EstadoOperativoCamion.DISPONIBLE);
		when(strategy.determinarEstado(camionActivo2)).thenReturn(EstadoOperativoCamion.EN_TRANSITO);

		FlotaResumenResponse resumen = service.obtenerResumen();

		assertThat(resumen.total()).isEqualTo(3L);
		assertThat(resumen.activos()).isEqualTo(2L);
		assertThat(resumen.inactivos()).isEqualTo(1L);
		assertThat(resumen.disponibles()).isEqualTo(1L);
		assertThat(resumen.enTransito()).isEqualTo(1L);
	}
}
