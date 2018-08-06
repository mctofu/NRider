package nrider.io;

import java.util.EventListener;

public interface IPerformanceDataListener extends EventListener {
    void handlePerformanceData(String identifier, PerformanceData data);
}
