package um.edu.ar.omnitrucks.simulacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import um.edu.ar.omnitrucks.common.geo.Coordenada;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.posicion.PosicionRepository;
import um.edu.ar.omnitrucks.posicion.PosicionService;
import um.edu.ar.omnitrucks.posicion.dto.PosicionRequest;
import um.edu.ar.omnitrucks.simulacion.estrategia.InterpolacionGeodesicaStrategy;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.ViajeRepository;
import um.edu.ar.omnitrucks.viaje.ViajeService;

@ExtendWith(MockitoExtension.class)
class SimuladorRecorridosServiceTest {

	@Mock
	private ViajeRepository viajeRepository;

	@Mock
	private ViajeService viajeService;

	@Mock
	private PosicionRepository posicionRepository;

	@Mock
	private PosicionService posicionService;

	@Captor
	private ArgumentCaptor<List<PosicionRequest>> posicionesCaptor;

	private SimuladorRecorridosService simulador;

	// Coordenadas
	// CABA: -34.6037, -58.3816
	// Rosario: -32.9468, -60.6393
	private final double cabaLat = -34.6037;
	private final double cabaLon = -58.3816;
	private final double rosarioLat = -32.9468;
	private final double rosarioLon = -60.6393;

	@BeforeEach
	void setUp() {
		// velocidad: 80 km/h, intervalo: 5s, umbralLlegada: 100 metros
		simulador = new SimuladorRecorridosService(
			viajeRepository,
			viajeService,
			posicionRepository,
			posicionService,
			new InterpolacionGeodesicaStrategy(),
			80.0,
			5,
			100.0
		);
	}

	@Test
	@DisplayName("No hace nada si no hay viajes en curso")
	void noHaceNadaSinViajesEnCurso() {
		when(viajeRepository.findAllByEstado(EstadoViaje.EN_CURSO)).thenReturn(List.of());

		simulador.simularPaso();

		verify(posicionService, never()).registrar(any(), anyList());
		verify(viajeService, never()).finalizar(any());
	}

	@Test
	@DisplayName("Avanza un viaje desde el origen si no tiene posiciones previas")
	void avanzaDesdeOrigenSiNoHayPosiciones() {
		Viaje viaje = new Viaje();
		viaje.setId(10L);
		viaje.setEstado(EstadoViaje.EN_CURSO);
		viaje.setOrigenLat(cabaLat);
		viaje.setOrigenLon(cabaLon);
		viaje.setDestinoLat(rosarioLat);
		viaje.setDestinoLon(rosarioLon);

		when(viajeRepository.findAllByEstado(EstadoViaje.EN_CURSO)).thenReturn(List.of(viaje));
		when(posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(10L)).thenReturn(Optional.empty());

		simulador.simularPaso();

		verify(posicionService).registrar(eq(10L), posicionesCaptor.capture());
		List<PosicionRequest> requests = posicionesCaptor.getValue();
		assertThat(requests).hasSize(1);

		PosicionRequest reg = requests.getFirst();
		assertThat(reg.velocidadKmh()).isEqualTo(80.0);
		assertThat(reg.rumboGrados()).isBetween(310.0, 325.0);

		// Distancia recorrida en 5 segundos a 80 km/h: ~111.11 metros
		Coordenada calculada = new Coordenada(reg.latitud(), reg.longitud());
		Coordenada origen = new Coordenada(cabaLat, cabaLon);
		assertThat(origen.distanciaMetros(calculada)).isCloseTo(111.11, within(5.0));

		verify(viajeService, never()).finalizar(10L);
	}

	@Test
	@DisplayName("Avanza un viaje desde su última posición registrada")
	void avanzaDesdeUltimaPosicion() {
		Viaje viaje = new Viaje();
		viaje.setId(10L);
		viaje.setEstado(EstadoViaje.EN_CURSO);
		viaje.setOrigenLat(cabaLat);
		viaje.setOrigenLon(cabaLon);
		viaje.setDestinoLat(rosarioLat);
		viaje.setDestinoLon(rosarioLon);

		// Supongamos que ya avanzó 50 km
		Coordenada puntoActual = new Coordenada(cabaLat, cabaLon).desplazar(50_000.0, 315.0);
		Posicion ultima = new Posicion();
		ultima.setId(99L);
		ultima.setLatitud(puntoActual.latitud());
		ultima.setLongitud(puntoActual.longitud());
		ultima.setRegistradoEn(Instant.now());

		when(viajeRepository.findAllByEstado(EstadoViaje.EN_CURSO)).thenReturn(List.of(viaje));
		when(posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(10L)).thenReturn(Optional.of(ultima));

		simulador.simularPaso();

		verify(posicionService).registrar(eq(10L), posicionesCaptor.capture());
		PosicionRequest reg = posicionesCaptor.getValue().getFirst();
		Coordenada siguiente = new Coordenada(reg.latitud(), reg.longitud());

		assertThat(puntoActual.distanciaMetros(siguiente)).isCloseTo(111.11, within(5.0));
		verify(viajeService, never()).finalizar(10L);
	}

	@Test
	@DisplayName("Finaliza el viaje cuando la distancia al destino es menor o igual al umbral de llegada")
	void finalizaViajeAlLlegarADestino() {
		Viaje viaje = new Viaje();
		viaje.setId(10L);
		viaje.setEstado(EstadoViaje.EN_CURSO);
		viaje.setOrigenLat(cabaLat);
		viaje.setOrigenLon(cabaLon);
		viaje.setDestinoLat(rosarioLat);
		viaje.setDestinoLon(rosarioLon);

		// Simulamos que el camión está a solo 50 metros del destino (umbral es 100m)
		Coordenada muyCerca = new Coordenada(rosarioLat, rosarioLon).desplazar(50.0, 180.0);
		Posicion ultima = new Posicion();
		ultima.setId(101L);
		ultima.setLatitud(muyCerca.latitud());
		ultima.setLongitud(muyCerca.longitud());
		ultima.setRegistradoEn(Instant.now());

		when(viajeRepository.findAllByEstado(EstadoViaje.EN_CURSO)).thenReturn(List.of(viaje));
		when(posicionRepository.findFirstByViajeIdOrderByRegistradoEnDescIdDesc(10L)).thenReturn(Optional.of(ultima));

		simulador.simularPaso();

		// Se registra la posición en destino y se finaliza el viaje
		verify(posicionService).registrar(eq(10L), posicionesCaptor.capture());
		PosicionRequest reg = posicionesCaptor.getValue().getFirst();
		assertThat(reg.latitud()).isEqualTo(rosarioLat);
		assertThat(reg.longitud()).isEqualTo(rosarioLon);
		assertThat(reg.velocidadKmh()).isEqualTo(0.0);

		verify(viajeService).finalizar(10L);
	}
}
