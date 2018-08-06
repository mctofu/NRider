package nrider.ride;

public interface IRide {
    enum Status {PENDING, READY, RUNNING, PAUSED, STOPPED}

    void start();

    void pause();

    void stop();

    Status getStatus();

    RideScript getScript();
}
