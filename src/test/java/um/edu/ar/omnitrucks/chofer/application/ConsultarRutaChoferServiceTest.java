package um.edu.ar.omnitrucks.chofer.application;

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
import um.edu.ar.omnitrucks.chofer.application.dto.RutaChoferResponse;
import um.edu.ar.omnitrucks.chofer.application.dto.ViajeChoferResumenResponse;
import um.edu.ar.omnitrucks.chofer.application.service.ConsultarRutaChoferService;
import um.edu.ar.omnitrucks.chofer.domain.port.ChoferRutaRepositoryPort;
import um.edu.ar.omnitrucks.common.error.AccesoDenegadoException;
import um.edu.ar.omnitrucks.common.error.CuentaInactivaException;
import um.edu.ar.omnitrucks.common.error.RecursoNoEncontradoException;
import um.edu.ar.omnitrucks.posicion.Posicion;
import um.edu.ar.omnitrucks.usuario.Rol;
import um.edu.ar.omnitrucks.usuario.Usuario;
import um.edu.ar.omnitrucks.usuario.domain.port.UsuarioRepositoryPort;
import um.edu.ar.omnitrucks.viaje.EstadoViaje;
import um.edu.ar.omnitrucks.viaje.Viaje;

@ExtendWith(MockitoExtension.class)
class ConsultarRutaChoferServiceTest {

	@Mock
	private ChoferRutaRepositoryPort choferRutaRepository;

	@Mock
	private UsuarioRepositoryPort usuarioRepository;

	@InjectMocks
	private ConsultarRutaChoferService service;

	private Usuario chofer1;
	private Usuario chofer2;
	private Usuario admin;
	private Usuario cliente;
	private Camion camion1;
	private Viaje viajeChofer1;
	private Posicion ultimaPosicion;

	@BeforeEach
	void setUp() {
		chofer1 = new Usuario();
		chofer1.setId(101L);
		chofer1.setEmail("chofer1@omnitrucks.com");
		chofer1.setNombre("Mario");
		chofer1.setApellido("Baracus");
		chofer1.setRol(Rol.CHOFER);
		chofer1.setActivo(true);

		chofer2 = new Usuario();
		chofer2.setId(102L);
		chofer2.setEmail("chofer2@omnitrucks.com");
		chofer2.setNombre("Carlos");
		chofer2.setApellido("Sainz");
		chofer2.setRol(Rol.CHOFER);
		chofer2.setActivo(true);

		admin = new Usuario();
		admin.setId(1L);
		admin.setEmail("admin@omnitrucks.com");
		admin.setRol(Rol.ADMIN);
		admin.setActivo(true);

		cliente = new Usuario();
		cliente.setId(301L);
		cliente.setEmail("cliente@omnitrucks.com");
		cliente.setRol(Rol.CLIENTE);
		cliente.setActivo(true);

		camion1 = new Camion();
		camion1.setId(51L);
		camion1.setPatente("AA123BB");
		camion1.setMarca("Scania");
		camion1.setModelo("R450");
		camion1.setAnio(2022);
		camion1.setCapacidadKg(35000);

		viajeChofer1 = new Viaje();
		viajeChofer1.setId(201L);
		viajeChofer1.setCodigoSeguimiento("TRK-RUTA001");
		viajeChofer1.setChofer(chofer1);
		viajeChofer1.setCamion(camion1);
		viajeChofer1.setOrigenNombre("Mendoza");
		viajeChofer1.setOrigenLat(-32.889);
		viajeChofer1.setOrigenLon(-68.845);
		viajeChofer1.setDestinoNombre("Rosario");
		viajeChofer1.setDestinoLat(-32.950);
		viajeChofer1.setDestinoLon(-60.650);
		viajeChofer1.setEstado(EstadoViaje.EN_CURSO);

		ultimaPosicion = new Posicion();
		ultimaPosicion.setId(901L);
		ultimaPosicion.setViaje(viajeChofer1);
		ultimaPosicion.setLatitud(-32.90);
		ultimaPosicion.setLongitud(-65.40);
		ultimaPosicion.setVelocidadKmh(79.0);
		ultimaPosicion.setRumboGrados(88.0);
		ultimaPosicion.setRegistradoEn(Instant.now());
	}

	@Test
	@DisplayName("consultarRutaActiva retorna la ruta y vehículo asignado al chofer")
	void consultarRutaActiva_conViajeAsignado_retornaRutaConVehiculoAsociado() {
		when(usuarioRepository.findByEmail(chofer1.getEmail())).thenReturn(Optional.of(chofer1));
		when(choferRutaRepository.buscarViajeActivoPorChoferId(101L)).thenReturn(Optional.of(viajeChofer1));
		when(choferRutaRepository.buscarUltimaPosicion(201L)).thenReturn(Optional.of(ultimaPosicion));
		when(choferRutaRepository.obtenerRecorrido(201L)).thenReturn(List.of(ultimaPosicion));

		Optional<RutaChoferResponse> resultado = service.consultarRutaActiva(chofer1.getEmail());

		assertThat(resultado).isPresent();
		RutaChoferResponse ruta = resultado.get();
		assertThat(ruta.viajeId()).isEqualTo(201L);
		assertThat(ruta.codigoSeguimiento()).isEqualTo("TRK-RUTA001");
		assertThat(ruta.estado()).isEqualTo(EstadoViaje.EN_CURSO);
		// Valida que el vehículo asociado sea exactamente el del chofer
		assertThat(ruta.vehiculo().patente()).isEqualTo("AA123BB");
		assertThat(ruta.vehiculo().marca()).isEqualTo("Scania");
		assertThat(ruta.origen().nombre()).isEqualTo("Mendoza");
		assertThat(ruta.destino().nombre()).isEqualTo("Rosario");
		assertThat(ruta.ultimaUbicacion().latitud()).isEqualTo(-32.90);
		assertThat(ruta.recorrido()).hasSize(1);
	}

	@Test
	@DisplayName("consultarRutaActiva sin viajes activos retorna Optional vacío")
	void consultarRutaActiva_sinViajeAsignado_retornaOptionalVacio() {
		when(usuarioRepository.findByEmail(chofer1.getEmail())).thenReturn(Optional.of(chofer1));
		when(choferRutaRepository.buscarViajeActivoPorChoferId(101L)).thenReturn(Optional.empty());

		Optional<RutaChoferResponse> resultado = service.consultarRutaActiva(chofer1.getEmail());

		assertThat(resultado).isEmpty();
	}

	@Test
	@DisplayName("consultarRutaPorViajeId permite al chofer consultar su propio viaje")
	void consultarRutaPorViajeId_choferAsignado_retornaRuta() {
		when(usuarioRepository.findByEmail(chofer1.getEmail())).thenReturn(Optional.of(chofer1));
		when(choferRutaRepository.buscarPorId(201L)).thenReturn(Optional.of(viajeChofer1));
		when(choferRutaRepository.buscarUltimaPosicion(201L)).thenReturn(Optional.of(ultimaPosicion));
		when(choferRutaRepository.obtenerRecorrido(201L)).thenReturn(List.of());

		RutaChoferResponse ruta = service.consultarRutaPorViajeId(201L, chofer1.getEmail());

		assertThat(ruta).isNotNull();
		assertThat(ruta.viajeId()).isEqualTo(201L);
		assertThat(ruta.vehiculo().patente()).isEqualTo("AA123BB");
	}

	@Test
	@DisplayName("consultarRutaPorViajeId cuando un chofer intenta acceder a la ruta de otro chofer lanza AccesoDenegadoException")
	void consultarRutaPorViajeId_otroChofer_lanzaAccesoDenegadoException() {
		when(usuarioRepository.findByEmail(chofer2.getEmail())).thenReturn(Optional.of(chofer2));
		when(choferRutaRepository.buscarPorId(201L)).thenReturn(Optional.of(viajeChofer1));

		assertThatThrownBy(() -> service.consultarRutaPorViajeId(201L, chofer2.getEmail()))
				.isInstanceOf(AccesoDenegadoException.class)
				.hasMessageContaining("No tiene permisos para acceder a la ruta de otro chofer");
	}

	@Test
	@DisplayName("consultarRutaPorViajeId como ADMIN permite supervisar la ruta")
	void consultarRutaPorViajeId_comoAdmin_permiteAcceso() {
		when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
		when(choferRutaRepository.buscarPorId(201L)).thenReturn(Optional.of(viajeChofer1));
		when(choferRutaRepository.buscarUltimaPosicion(201L)).thenReturn(Optional.empty());
		when(choferRutaRepository.obtenerRecorrido(201L)).thenReturn(List.of());

		RutaChoferResponse ruta = service.consultarRutaPorViajeId(201L, admin.getEmail());

		assertThat(ruta).isNotNull();
		assertThat(ruta.viajeId()).isEqualTo(201L);
	}

	@Test
	@DisplayName("consultarRutaActiva para un usuario con rol CLIENTE lanza AccesoDenegadoException")
	void consultarRutaActiva_comoCliente_lanzaAccesoDenegadoException() {
		when(usuarioRepository.findByEmail(cliente.getEmail())).thenReturn(Optional.of(cliente));

		assertThatThrownBy(() -> service.consultarRutaActiva(cliente.getEmail()))
				.isInstanceOf(AccesoDenegadoException.class)
				.hasMessageContaining("Rol no autorizado para acceder al portal de choferes");
	}

	@Test
	@DisplayName("consultarRutaActiva para usuario inactivo lanza CuentaInactivaException")
	void consultarRutaActiva_usuarioInactivo_lanzaCuentaInactivaException() {
		chofer1.setActivo(false);
		when(usuarioRepository.findByEmail(chofer1.getEmail())).thenReturn(Optional.of(chofer1));

		assertThatThrownBy(() -> service.consultarRutaActiva(chofer1.getEmail()))
				.isInstanceOf(CuentaInactivaException.class);
	}

	@Test
	@DisplayName("listarMisViajes retorna únicamente los viajes asignados al chofer")
	void listarMisViajes_retornaSoloViajesDelChofer() {
		when(usuarioRepository.findByEmail(chofer1.getEmail())).thenReturn(Optional.of(chofer1));
		when(choferRutaRepository.listarViajesPorChoferId(101L)).thenReturn(List.of(viajeChofer1));

		List<ViajeChoferResumenResponse> viajes = service.listarMisViajes(chofer1.getEmail());

		assertThat(viajes).hasSize(1);
		assertThat(viajes.get(0).viajeId()).isEqualTo(201L);
		assertThat(viajes.get(0).patenteCamion()).isEqualTo("AA123BB");
		verify(choferRutaRepository).listarViajesPorChoferId(101L);
	}

}
