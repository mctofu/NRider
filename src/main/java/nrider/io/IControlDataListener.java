package nrider.io;

import java.util.EventListener;

public interface IControlDataListener extends EventListener {
    void handleControlData(String identifier, ControlData data);
}
