package nrider.core;

import nrider.ride.IRide;

import java.util.EventListener;

public interface IWorkoutListener extends EventListener {
    void handleLoadAdjust(String riderId, RideLoad newLoad);

    void handleAddRider(Rider rider);

    void handleRiderThresholdAdjust(String riderId, double newThreshold);

    void handleRideLoaded(IRide ride);

    void handleRideTimeUpdate(long rideTime);

    void handleAddRiderAlert(String riderId, WorkoutSession.RiderAlertType type);

    void handleRemoveRiderAlert(String riderId, WorkoutSession.RiderAlertType type);

    void handleRideStatusUpdate(IRide.Status status);
}
