package nrider.io;

public interface IControlDataSource {
    String getIdentifier();

    void addControlDataListener(IControlDataListener listener);
}
