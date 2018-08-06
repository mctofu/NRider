package nrider.io;

import java.io.IOException;

public interface IWorkoutController {
    enum TrainerMode {ERG, GRADIENT}

    String getType();

    String getIdentifier();

    void setLoad(double load);

    double getLoad();

    void setMode(TrainerMode mode);

    TrainerMode getMode();

    // temporarily disconnect
    void disconnect() throws IOException;

    void connect();

    // disconnect and release all resources
    void close() throws IOException;
}
