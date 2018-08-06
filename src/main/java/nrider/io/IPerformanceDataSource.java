package nrider.io;

public interface IPerformanceDataSource {
    String getIdentifier();

    void addPerformanceDataListener(IPerformanceDataListener listener);
}
