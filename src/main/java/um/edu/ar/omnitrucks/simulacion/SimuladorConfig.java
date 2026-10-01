package um.edu.ar.omnitrucks.simulacion;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita el scheduling de tareas periódicas únicamente cuando el perfil 'sim' está activo.
 */
@Configuration
@Profile("sim")
@EnableScheduling
public class SimuladorConfig {
}
