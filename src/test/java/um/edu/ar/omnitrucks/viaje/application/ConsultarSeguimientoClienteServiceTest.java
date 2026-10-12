package um.edu.ar.omnitrucks.viaje.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.common.error.AccesoDenegadoException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;
import um.edu.ar.omnitrucks.viaje.application.dto.EnvioClienteResumenResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.SeguimientoClienteResponse;
import um.edu.ar.omnitrucks.viaje.application.dto.UbicacionSeguimientoResponse;
import um.edu.ar.omnitrucks.viaje.application.service.ConsultarSeguimientoClienteService;
import um.edu.ar.omnitrucks.viaje.domain.port.ViajeSeguimientoRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ConsultarSeguimientoClienteServiceTest {

	@Mock
	private ViajeSeguimientoRepositoryPort viajeSeguimientoRepository;

	@Mock
	private UsuarioRepositoryPort usuarioRepository;

	@InjectMocks
	private ConsultarSeguimientoClienteService service;

	private Usuario clienteA;
	private Usuario clienteB;
	private Usuario admin;
	private Usuario chofer;
	private Viaje viajeClienteA;
	private Posicion posicionActual;

	@BeforeEach
	void setUp() {
		clienteA = new Usuario();
		clienteA.setId(10L);
		clienteA.setEmail("cliente.a@empresa.com");
		clienteA.setNombre("Cliente");
		clienteA.setApellido("Alfa");
		clienteA.setRol(Rol.CLIENTE);
		clienteA.setActivo(true);

		clienteB = new Usuario();
		clienteB.setId(20L);
		clienteB.setEmail("cliente.b@empresa.com");
		clienteB.setNombre("Cliente");
		clienteB.setApellido("Beta");
		clienteB.setRol(Rol.CLIENTE);
		clienteB.setActivo(true);

		admin = new Usuario();
		admin.setId(1L);
		admin.setEmail("admin@omnitrucks.com");
		admin.setRol(Rol.ADMIN);
		admin.setActivo(true);

		chofer = new Usuario();
		chofer.setId(2L);
		chofer.setEmail("chofer@omnitrucks.com");
		chofer.setNombre("Roberto");
		chofer.setApellido("Gomez");
		chofer.setRol(Rol.CHOFER);
		chofer.setActivo(true);

		Camion camion = new Camion();
		camion.setId(100L);
		camion.setPatente("AA111BB");
		camion.setMarca("Scania");
		camion.setModelo("R450");
		camion.setAnio(2022);
		camion.setCapacidadKg(35000);

		viajeClienteA = new Viaje();
		viajeClienteA.setId(500L);
		viajeClienteA.setCodigoSeguimiento("TRK-ALFA001");
		viajeClienteA.setCliente(clienteA);
		viajeClienteA.setChofer(chofer);
		viajeClienteA.setCamion(camion);
		viajeClienteA.setEstado(EstadoViaje.EN_CURSO);
		viajeClienteA.setOrigenNombre("Mendoza");
		viajeClienteA.setOrigenLat(-32.889);
		viajeClienteA.setOrigenLon(-68.845);
		viajeClienteA.setDestinoNombre("Buenos Aires");
		viajeClienteA.setDestinoLat(-34.603);
		viajeClienteA.setDestinoLon(-58.381);
		viajeClienteA.setDescripcionCarga("Cajas de Vino Malbec");

		posicionActual = new Posicion();
		posicionActual.setId(900L);
		posicionActual.setViaje(viajeClienteA);
		posicionActual.setLatitud(-33.5);
		posicionActual.setLongitud(-65.2);
		posicionActual.setVelocidadKmh(82.5);
		posicionActual.setRumboGrados(95.0);
		posicionActual.setRegistradoEn(Instant.now());
	}

	@Test
	@DisplayName("consultarPorCodigo con cliente asignado retorna DTO público sin datos sensibles de la flota")
	void consultarPorCodigo_clienteAsignado_retornaInformacionPublicaSinDatosSensibles() {
		when(usuarioRepository.findByEmail(clienteA.getEmail())).thenReturn(Optional.of(clienteA));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-ALFA001")).thenReturn(Optional.of(viajeClienteA));
		when(viajeSeguimientoRepository.buscarUltimaPosicion(500L)).thenReturn(Optional.of(posicionActual));

		SeguimientoClienteResponse respuesta = service.consultarPorCodigo("TRK-ALFA001", clienteA.getEmail());

		assertThat(respuesta).isNotNull();
		assertThat(respuesta.codigoSeguimiento()).isEqualTo("TRK-ALFA001");
		assertThat(respuesta.estado()).isEqualTo(EstadoViaje.EN_CURSO);
		assertThat(respuesta.descripcionCarga()).isEqualTo("Cajas de Vino Malbec");
		// Verifica datos públicos del vehículo (oculta id y capacidad)
		assertThat(respuesta.vehiculo().patente()).isEqualTo("AA111BB");
		assertThat(respuesta.vehiculo().marca()).isEqualTo("Scania");
		// Verifica datos públicos del chofer (oculta id y email)
		assertThat(respuesta.chofer().nombre()).isEqualTo("Roberto Gomez");
		// Verifica ubicación actual
		assertThat(respuesta.ubicacionActual()).isNotNull();
		assertThat(respuesta.ubicacionActual().latitud()).isEqualTo(-33.5);
		assertThat(respuesta.ubicacionActual().longitud()).isEqualTo(-65.2);
	}

	@Test
	@DisplayName("consultarPorCodigo cuando el viaje no tiene cliente previo asocia automáticamente el viaje al cliente")
	void consultarPorCodigo_sinClientePrevio_vinculaAutomaticamenteAlCliente() {
		viajeClienteA.setCliente(null);
		when(usuarioRepository.findByEmail(clienteA.getEmail())).thenReturn(Optional.of(clienteA));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-ALFA001")).thenReturn(Optional.of(viajeClienteA));
		when(viajeSeguimientoRepository.buscarUltimaPosicion(500L)).thenReturn(Optional.of(posicionActual));

		SeguimientoClienteResponse respuesta = service.consultarPorCodigo("TRK-ALFA001", clienteA.getEmail());

		assertThat(respuesta).isNotNull();
		assertThat(respuesta.codigoSeguimiento()).isEqualTo("TRK-ALFA001");
		assertThat(viajeClienteA.getCliente()).isEqualTo(clienteA);
	}

	@Test
	@DisplayName("consultarPorCodigo desde un cliente distinto lanza AccesoDenegadoException")
	void consultarPorCodigo_clienteDiferente_lanzaAccesoDenegadoException() {
		when(usuarioRepository.findByEmail(clienteB.getEmail())).thenReturn(Optional.of(clienteB));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-ALFA001")).thenReturn(Optional.of(viajeClienteA));

		assertThatThrownBy(() -> service.consultarPorCodigo("TRK-ALFA001", clienteB.getEmail()))
				.isInstanceOf(AccesoDenegadoException.class)
				.hasMessageContaining("No tiene permisos para consultar este envío");
	}

	@Test
	@DisplayName("consultarPorCodigo con código inexistente lanza RecursoNoEncontradoException")
	void consultarPorCodigo_envioInexistente_lanzaRecursoNoEncontradoException() {
		when(usuarioRepository.findByEmail(clienteA.getEmail())).thenReturn(Optional.of(clienteA));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-INEXISTENTE")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.consultarPorCodigo("TRK-INEXISTENTE", clienteA.getEmail()))
				.isInstanceOf(RecursoNoEncontradoException.class)
				.hasMessageContaining("envío con código");
	}

	@Test
	@DisplayName("consultarPorCodigo como ADMIN permite supervisar el envío de cualquier cliente")
	void consultarPorCodigo_comoAdmin_permiteAccesoACualquierEnvio() {
		when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-ALFA001")).thenReturn(Optional.of(viajeClienteA));
		when(viajeSeguimientoRepository.buscarUltimaPosicion(500L)).thenReturn(Optional.of(posicionActual));

		SeguimientoClienteResponse respuesta = service.consultarPorCodigo("TRK-ALFA001", admin.getEmail());

		assertThat(respuesta).isNotNull();
		assertThat(respuesta.codigoSeguimiento()).isEqualTo("TRK-ALFA001");
	}

	@Test
	@DisplayName("consultarPorCodigo como CHOFER es denegado con AccesoDenegadoException")
	void consultarPorCodigo_comoChofer_lanzaAccesoDenegadoException() {
		when(usuarioRepository.findByEmail(chofer.getEmail())).thenReturn(Optional.of(chofer));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-ALFA001")).thenReturn(Optional.of(viajeClienteA));

		assertThatThrownBy(() -> service.consultarPorCodigo("TRK-ALFA001", chofer.getEmail()))
				.isInstanceOf(AccesoDenegadoException.class)
				.hasMessageContaining("Los choferes no tienen acceso a la vista de cliente");
	}

	@Test
	@DisplayName("consultarPorCodigo con usuario inactivo lanza CuentaInactivaException")
	void consultarPorCodigo_usuarioInactivo_lanzaCuentaInactivaException() {
		clienteA.setActivo(false);
		when(usuarioRepository.findByEmail(clienteA.getEmail())).thenReturn(Optional.of(clienteA));

		assertThatThrownBy(() -> service.consultarPorCodigo("TRK-ALFA001", clienteA.getEmail()))
				.isInstanceOf(CuentaInactivaException.class);
	}

	@Test
	@DisplayName("listarEnviosCliente retorna únicamente los envíos asignados al cliente autenticado")
	void listarEnviosCliente_retornaSoloEnviosAsignadosAlCliente() {
		when(usuarioRepository.findByEmail(clienteA.getEmail())).thenReturn(Optional.of(clienteA));
		when(viajeSeguimientoRepository.listarPorClienteId(clienteA.getId())).thenReturn(List.of(viajeClienteA));

		List<EnvioClienteResumenResponse> envios = service.listarEnviosCliente(clienteA.getEmail());

		assertThat(envios).hasSize(1);
		assertThat(envios.get(0).codigoSeguimiento()).isEqualTo("TRK-ALFA001");
		verify(viajeSeguimientoRepository).listarPorClienteId(clienteA.getId());
	}

	@Test
	@DisplayName("consultarUbicacionEnVivo retorna posición en tiempo real para el cliente asignado")
	void consultarUbicacionEnVivo_clienteAsignado_retornaPosicion() {
		when(usuarioRepository.findByEmail(clienteA.getEmail())).thenReturn(Optional.of(clienteA));
		when(viajeSeguimientoRepository.buscarPorCodigoSeguimiento("TRK-ALFA001")).thenReturn(Optional.of(viajeClienteA));
		when(viajeSeguimientoRepository.buscarUltimaPosicion(500L)).thenReturn(Optional.of(posicionActual));

		UbicacionSeguimientoResponse ubicacion = service.consultarUbicacionEnVivo("TRK-ALFA001", clienteA.getEmail());

		assertThat(ubicacion).isNotNull();
		assertThat(ubicacion.codigoSeguimiento()).isEqualTo("TRK-ALFA001");
		assertThat(ubicacion.latitud()).isEqualTo(-33.5);
		assertThat(ubicacion.longitud()).isEqualTo(-65.2);
		assertThat(ubicacion.velocidadKmh()).isEqualTo(82.5);
	}

}
