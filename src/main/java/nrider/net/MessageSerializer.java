package nrider.net;

import nrider.core.WorkoutSession;
import nrider.io.PerformanceData;
import nrider.ride.IRide;

import java.io.PrintWriter;

public class MessageSerializer {
    public enum MessageType {
        PERFORMANCE_DATA,
        RIDER_THRESHOLD_ADJUST,
        ADD_RIDER_ALERT,
        REMOVE_RIDER_ALERT,
        RIDE_STATUS_UPDATE
    }

    private PrintWriter _writer;

    public MessageSerializer(PrintWriter writer) {
        _writer = writer;
    }

    public void sendPerformanceData(String identifier, PerformanceData data) {
        send(MessageType.PERFORMANCE_DATA + ":" + identifier + ":" + data.getType() + ":" + data.getValue());
    }

    public void sendRiderThresholdAdjust(String riderId, double newThreshold) {
        send(MessageType.RIDER_THRESHOLD_ADJUST + ":" + riderId + ":" + newThreshold);
    }


    public void sendAddRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {
        send(MessageType.ADD_RIDER_ALERT + ":" + riderId + ":" + type);
    }


    public void sendRemoveRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {
        send(MessageType.REMOVE_RIDER_ALERT + ":" + riderId + ":" + type);
    }


    public void sendRideStatusUpdate(IRide.Status status) {
        send(MessageType.RIDE_STATUS_UPDATE + ":" + status);
    }

    private void send(String message) {
        _writer.println(message);
        _writer.flush();
    }

}
