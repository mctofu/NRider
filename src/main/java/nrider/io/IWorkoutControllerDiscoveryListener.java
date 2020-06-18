package nrider.io;

/**
 * Provides notification when a new workout controller is discovered
 */
public interface IWorkoutControllerDiscoveryListener {
    void handleWorkoutController(IWorkoutController controller);
}
