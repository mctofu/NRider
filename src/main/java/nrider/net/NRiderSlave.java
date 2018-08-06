package nrider.net;

import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import nrider.ride.IRide;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;

public class NRiderSlave extends NetSource {
    private final static Logger LOG = Logger.getLogger(NRiderSlave.class);

    private Socket _socket;
    private String _host;
    private int _port;
    private PrintWriter _writer;
    private MessageSerializer _serializer;
    private BufferedReader _reader;
    private EventPublisher<IPerformanceDataListener> _dataPublisher = EventPublisher.singleThreadPublisher();
    private HashSet<String> _identifiers = new HashSet<String>();

    public NRiderSlave(String host, int port) {
        _host = host;
        _port = port;
    }

    public String getIdentifier() {
        return "NRiderSlave";
    }

    public void connect() throws IOException {
        _socket = new Socket(_host, _port);
        _writer = new PrintWriter(_socket.getOutputStream());
        _serializer = new MessageSerializer(_writer);
        _reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        new Thread(new InputHandler()).start();
    }

    public void close() throws IOException {
        _reader.close();
        _writer.close();
        _socket.close();
    }

    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _dataPublisher.addListener(listener);
    }

    public void handlePerformanceData(String identifier, PerformanceData data) {
        if (!_identifiers.contains(identifier)) {
            _serializer.sendPerformanceData(identifier, data);
        }
    }

    public void handleLoadAdjust(String riderId, RideLoad newLoad) {

    }

    public void handleAddRider(Rider rider) {

    }

    public void handleRiderThresholdAdjust(String riderId, double newThreshold) {
        if (!_identifiers.contains(riderId)) {
            _serializer.sendRiderThresholdAdjust(riderId, newThreshold);
        }
    }

    public void handleRideLoaded(IRide ride) {
        // ignore, server controlled
    }

    public void handleRideTimeUpdate(long rideTime) {
        // ignore, server controlled
    }

    public void handleAddRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {
        if (!_identifiers.contains(riderId)) {
            _serializer.sendAddRiderAlert(riderId, type);
        }
    }

    public void handleRemoveRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {
        if (!_identifiers.contains(riderId)) {
            _serializer.sendRemoveRiderAlert(riderId, type);
        }
    }

    public void handleRideStatusUpdate(IRide.Status status) {
        // ignore, server controls ride status
    }

    public class InputHandler implements Runnable {

        public void run() {
            try {
                while (true) {
                    String input = _reader.readLine();
                    handleClientData(input, _writer);
                }
            } catch (IOException e) {
                LOG.error("error reading from server", e);
            }
        }

        private void handleClientData(String line, PrintWriter writer) {
            LOG.info("recieved:" + line);
            String[] data = line.split(":");

            if (data.length > 0) {
                MessageSerializer.MessageType type = MessageSerializer.MessageType.valueOf(data[0]);
                switch (type) {
                    case PERFORMANCE_DATA:
                        final String identifier = data[1];
                        final PerformanceData pData = new PerformanceData(PerformanceData.Type.valueOf(data[2]), Float.valueOf(data[3]));
                        if (!_identifiers.contains(identifier)) {
                            _identifiers.add(identifier);
                        }
                        _dataPublisher.publishEvent(new IEvent<IPerformanceDataListener>() {
                            public void trigger(IPerformanceDataListener target) {
                                target.handlePerformanceData(identifier, pData);
                            }
                        });
                        break;
                    case RIDE_STATUS_UPDATE:
                        switch (IRide.Status.valueOf(data[1])) {
                            case RUNNING:
                                WorkoutSession.instance().startRide();
                                break;
                            case PAUSED:
                                WorkoutSession.instance().pauseRide();
                        }
                        break;
                    case RIDER_THRESHOLD_ADJUST:
                        WorkoutSession.instance().setRiderThreshold(data[1], Double.valueOf(data[2]).intValue());
                        break;
                }
            }
        }
    }
}
