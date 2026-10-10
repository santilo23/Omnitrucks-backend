package um.edu.ar.omnitrucks.camion.domain.strategy;

import um.edu.ar.omnitrucks.camion.Camion;
import um.edu.ar.omnitrucks.camion.domain.model.EstadoOperativoCamion;

/**
 * Patrón Strategy para calcular y clasificar dinámicamente el estado
 * operativo de los camiones de la flota.
 */
public interface DeterminadorEstadoCamionStrategy {

	EstadoOperativoCamion determinarEstado(Camion camion);

}
