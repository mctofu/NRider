package nrider;

import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import nrider.ride.IRide;
import nrider.ui.FontScalingLabel;
import nrider.ui.PerformanceStatView;
import nrider.ui.RecentPerformanceView;
import nrider.ui.RideScriptView;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Really basic UI for debugging.
 */
public class NRiderClient implements IPerformanceDataListener, IWorkoutListener {
    private static final String MAIN_REF_TEXT = "Calibration__";
    private static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 30);

    private JFrame _window;
    private RiderListView _riderListView;
    private JLabel _workoutLoad;
    private RideScriptView _rideScriptView;
    private JLabel _rideTime;

    public void start() {
        SwingUtilities.invokeLater(this::init);
    }

    private void init() {
        _window = new JFrame();
        _window.setSize(1600, 800);

        Container content = _window.getContentPane();
        content.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = .5;

        c.gridx = 0;
        c.gridy = 0;
        c.weighty = .05;
        c.anchor = GridBagConstraints.CENTER;
        _rideTime = createLabel("00:00:00", MAIN_REF_TEXT);
        _rideTime.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(_rideTime,c );

        c.gridy = 1;
        c.weighty = .05;
        _workoutLoad = createLabel("Workout Load: 0%", MAIN_REF_TEXT);
        _workoutLoad.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(_workoutLoad, c);

        c.gridy = 2;
        c.weighty = .3;
        c.anchor = GridBagConstraints.CENTER;
        _rideScriptView = new RideScriptView();
        content.add(_rideScriptView, c);

        c.gridy = 3;
        c.anchor = GridBagConstraints.SOUTH;
        c.weighty = .6;
        c.insets = new Insets(50, 75, 75, 75);
        _riderListView = new RiderListView();
        content.add(_riderListView.getContainer(), c);
        _window.setVisible(true);
    }

    public void handleRideLoaded(final IRide ride) {
        SwingUtilities.invokeLater(() -> _rideScriptView.setRideScript(ride.getScript()));
    }

    public void handlePerformanceData(final String identifier, final PerformanceData data) {
        SwingUtilities.invokeLater(() -> _riderListView.handlePerformanceData(identifier, data));
    }

    public void handleLoadAdjust(final String riderId, final RideLoad newLoad) {
        SwingUtilities.invokeLater(() -> _workoutLoad.setText("Workout Load:" + newLoad.toString()));
    }

    public void handleAddRider(final Rider rider) {
        SwingUtilities.invokeLater(() -> {
            _riderListView.addRider(rider);
            _window.getRootPane().revalidate();
        });
    }

    public void handleRiderThresholdAdjust(final String identifier, final double newThreshold) {
        SwingUtilities.invokeLater(() -> _riderListView.handleRiderThresholdAdjust(identifier, newThreshold));
    }

    private void setRideTime(long time) {
        DecimalFormat format = new DecimalFormat("00");
        long totalSeconds = time / 1000;
        String seconds = format.format((int) (totalSeconds % 60));
        String minutes = format.format((int) ((totalSeconds % 3600) / 60));
        String hours = format.format((int) (totalSeconds / 3600));

        _rideTime.setText(hours + ":" + minutes + ":" + seconds);
    }

    public void handleRideTimeUpdate(final long rideTime) {
        SwingUtilities.invokeLater(() -> {
            setRideTime(rideTime);
            _rideScriptView.setRideTime(rideTime);
        });
    }

    public void handleRideStatusUpdate(IRide.Status status) {

    }

    public void handleAddRiderAlert(final String identifier, final WorkoutSession.RiderAlertType alert) {
        SwingUtilities.invokeLater(() -> _riderListView.handleAddRiderAlert(identifier, alert));
    }

    public void handleRemoveRiderAlert(final String identifier, final WorkoutSession.RiderAlertType alert) {
        SwingUtilities.invokeLater(() -> _riderListView.handleRemoveRiderAlert(identifier, alert));
    }

    private JLabel createLabel(String text, String refText) {
        return createLabel(text, refText, true);
    }

    private FontScalingLabel createLabel(String text, String refText, boolean visible) {
        FontScalingLabel label = new FontScalingLabel(text, refText, 30, 70);
        label.setFont(DEFAULT_FONT);
        label.setVisible(visible);
        return label;
    }

    class RiderListView {
        private final Container _container = new JPanel(new GridBagLayout());
        private final HashMap<String, RiderView> _riderMap = new HashMap<>();
        private final JLabel _riderName;
        private final JLabel _threshold;
        private final JLabel _alert;
        private final JLabel _speed;
        private final JLabel _cadence;
        private final JLabel _power;
        private final JLabel _hr;
        private final JLabel _extHr;
        private final JLabel _extCadence;
        private final JLabel _extPower;
        private final JLabel _calibration;

        public RiderListView() {
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = .2;
            c.weighty = .2;
            c.fill = GridBagConstraints.BOTH;

            c.gridx = 0;
            _riderName = createLabel("Name", MAIN_REF_TEXT);
            c.gridy = 0;
            _container.add(_riderName, c);

            _threshold = createLabel("Threshold", MAIN_REF_TEXT);
            c.gridy = 1;
            _container.add(_threshold, c);

            _alert = createLabel("", MAIN_REF_TEXT);
            c.gridy = 2;
            _container.add(_alert, c);

            _speed = createLabel("Speed", MAIN_REF_TEXT);
            c.gridy = 3;
            _container.add(_speed, c);

            _cadence = createLabel("Cadence", MAIN_REF_TEXT, false);
            c.gridy = 4;
            _container.add(_cadence, c);

            _power = createLabel("Power", MAIN_REF_TEXT);
            c.gridy = 5;
            _container.add(_power, c);

            _hr = createLabel("HR", MAIN_REF_TEXT, false);
            c.gridy = 6;
            _container.add(_hr, c);

            _extHr = createLabel("Ext HR", MAIN_REF_TEXT, false);
            c.gridy = 7;
            _container.add(_extHr, c);

            _extCadence = createLabel("Ext Cadence", MAIN_REF_TEXT, false);
            c.gridy = 8;
            _container.add(_extCadence, c);

            _extPower = createLabel("Ext Power", MAIN_REF_TEXT, false);
            c.gridy = 9;
            _container.add(_extPower, c);

            _calibration = createLabel("Calibration", MAIN_REF_TEXT);
            c.gridy = 10;
            _container.add(_calibration, c);
        }

        public Container getContainer() {
            return _container;
        }

        public void addRider(Rider rider) {
            RiderView riderView = new RiderView(rider, _container, _riderMap.size() * 2 + 1);
            _riderMap.put(rider.getIdentifier(), riderView);
        }

        public void handlePerformanceData(String identifier, final PerformanceData data) {
            RiderView riderView = _riderMap.get(identifier);
            switch (data.getType()) {
                case POWER:
                    riderView.setPower(data.getValue());
                    break;
                case CADENCE:
                    _cadence.setVisible(true);
                    riderView.setCadence(data.getValue());
                    break;
                case SPEED:
                    // convert m/s to mph
                    riderView.setSpeed((float) (data.getValue() * 2.237));
                    break;
                case HEART_RATE:
                    _hr.setVisible(true);
                    riderView.setHeartRate(data.getValue());
                    break;
                case EXT_HEART_RATE:
                    _extHr.setVisible(true);
                    riderView.setExtHeartRate(data.getValue());
                    break;
                case EXT_CADENCE:
                    _extCadence.setVisible(true);
                    riderView.setExtCadence(data.getValue());
                    break;
                case EXT_POWER:
                    _extPower.setVisible(true);
                    riderView.setExtPower(data.getValue());
                    break;
                case CALIBRATION:
                    riderView.setCalibration(data.getValue());
                    break;
            }
        }

        public void handleAddRiderAlert(String identifier, WorkoutSession.RiderAlertType alert) {
            _riderMap.get(identifier).addAlert(alert);
        }

        public void handleRemoveRiderAlert(String identifier, WorkoutSession.RiderAlertType alert) {
            _riderMap.get(identifier).removeAlert(alert);
        }

        public void handleRiderThresholdAdjust(String identifier, double newThreshold) {
            _riderMap.get(identifier).setThreshold(newThreshold);
        }
    }

    class RiderView {
        private final Container _container;
        private final JLabel _name;
        private final PerformanceStatView _speed;
        private final PerformanceStatView _cadence;
        private final PerformanceStatView _power;
        private final JLabel _riderThreshold;
        private final PerformanceStatView _heartRate;
        private final PerformanceStatView _extHeartRate;
        private final PerformanceStatView _extCadence;
        private final PerformanceStatView _extPower;
        private final JLabel _calibration;
        private final JLabel _alert;
        private final HashSet<WorkoutSession.RiderAlertType> _alerts = new HashSet<>();
        private final Rider _rider;

        public RiderView(Rider rider, Container container, int columnNumber) {
            _container = container;
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = .2;
            c.weighty = .2;
            c.fill = GridBagConstraints.BOTH;
            c.ipadx = 20;

            c.gridx = columnNumber;

            _name = createLabel(rider.getName(), MAIN_REF_TEXT);
            _name.setHorizontalAlignment(SwingConstants.CENTER);
            c.gridy = 0;
            c.gridwidth = 2;
            _container.add(_name, c);

            _riderThreshold = createLabel(Integer.toString(rider.getThresholdPower()), PerformanceStatView.METRIC_REF_TEXT);
            _riderThreshold.setHorizontalAlignment(SwingConstants.CENTER);
            c.gridy = 1;
            c.gridwidth = 2;
            _container.add(_riderThreshold, c);

            _alert = createLabel("", MAIN_REF_TEXT, false);
            _alert.setBackground(Color.YELLOW);
            _alert.setForeground(Color.ORANGE);
            c.gridy = 2;
            c.gridwidth = 2;
            _container.add(_alert, c);

            c.gridwidth = 1;

            _speed = new PerformanceStatView(10, 30, 60, new DecimalFormat("0.0"));
            c.gridy = 3;
            addPerformanceStatView(_speed, c);

            _cadence = new PerformanceStatView(45, 130, 60, new DecimalFormat("0"));
            c.gridy = 4;
            _cadence.setVisible(false);
            addPerformanceStatView(_cadence, c);

            _power = new PerformanceStatView(0, rider.getThresholdPower() * 1.1, 60, new DecimalFormat("0"));
            c.gridy = 5;
            addPerformanceStatView(_power, c);

            _heartRate = new PerformanceStatView(50, 220, 60, new DecimalFormat("0"));
            c.gridy = 6;
            _heartRate.setVisible(false);
            addPerformanceStatView(_heartRate, c);

            _extHeartRate = new PerformanceStatView(50, 220, 60, new DecimalFormat("0"));
            c.gridy = 7;
            _extHeartRate.setVisible(false);
            addPerformanceStatView(_extHeartRate, c);

            _extCadence = new PerformanceStatView(45, 130, 60, new DecimalFormat("0"));
            c.gridy = 8;
            _extCadence.setVisible(false);
            addPerformanceStatView(_extCadence, c);

            _extPower = new PerformanceStatView(0, rider.getThresholdPower() * 1.1, 60, new DecimalFormat("0"));
            c.gridy = 9;
            _extPower.setVisible(false);
            addPerformanceStatView(_extPower, c);

            _calibration = createLabel("", MAIN_REF_TEXT, false);
            _calibration.setHorizontalAlignment(SwingConstants.CENTER);
            c.gridy = 10;
            c.gridwidth = 2;
            _container.add(_calibration, c);

            _rider = rider;

        }

        private void addPerformanceStatView(PerformanceStatView perf, GridBagConstraints c) {
            double weightx = c.weightx;
            int gridX = c.gridx;
            _container.add(perf.getLabel(), c);
            c.gridx = gridX + 1;
            c.weightx = weightx + .2;
            _container.add(perf.getGraph(), c);
            c.gridx = gridX;
            c.weightx = weightx;
        }

        public void setSpeed(float speed) {
            RecentPerformanceView.DataType type;
            if (speed > 25) {
                type = RecentPerformanceView.DataType.EXTREME;
            } else if (speed < 19) {
                type = RecentPerformanceView.DataType.WARNING;
            } else {
                type = RecentPerformanceView.DataType.NORMAL;
            }
            _speed.updateValue(speed, type);
        }

        public void setCadence(float cadence) {
            _cadence.setVisible(true);
            _cadence.updateValue(cadence, RecentPerformanceView.DataType.NORMAL);
        }

        public void setPower(float power) {
            RecentPerformanceView.DataType type;
            if (power > _rider.getThresholdPower() * 1.1) {
                type = RecentPerformanceView.DataType.EXTREME;
            } else if (power > _rider.getThresholdPower() * .9) {
                type = RecentPerformanceView.DataType.WARNING;
            } else {
                type = RecentPerformanceView.DataType.NORMAL;
            }
            _power.updateValue(power, type);
        }

        public void setThreshold(double threshold) {
            _riderThreshold.setText(new DecimalFormat("0").format(threshold));
        }

        public void setHeartRate(float hr) {
            _heartRate.setVisible(true);
            _heartRate.updateValue(hr, RecentPerformanceView.DataType.NORMAL);
        }

        public void setExtHeartRate(float hr) {
            _extHeartRate.setVisible(true);
            _extHeartRate.updateValue(hr, RecentPerformanceView.DataType.NORMAL);
        }

        public void setExtCadence(float cadence) {
            _extCadence.setVisible(true);
            _extCadence.updateValue(cadence, RecentPerformanceView.DataType.NORMAL);
        }

        public void setExtPower(float power) {
            _extPower.setVisible(true);
            _extPower.updateValue(power, RecentPerformanceView.DataType.NORMAL);
        }

        public void setCalibration(float calibration) {
            _calibration.setVisible(true);
            _calibration.setText(new DecimalFormat("0.00").format(calibration));
        }

        public void addAlert(WorkoutSession.RiderAlertType type) {
            _alerts.add(type);

            renderAlert();
        }


        public void removeAlert(WorkoutSession.RiderAlertType type) {
            _alerts.remove(type);

            renderAlert();
        }

        private void renderAlert() {
            StringBuilder sb = new StringBuilder();
            for (WorkoutSession.RiderAlertType alert : _alerts) {
                sb.append(alert.getShortName());
                sb.append(" ");
            }
            _alert.setText(sb.toString());

            _alert.setVisible(_alerts.size() > 0);
        }
    }
}
