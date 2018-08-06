package nrider.ui;

import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Simple UI for mapping devices to users
 */
public class DeviceMapper implements IPerformanceDataListener {
    private static Font _bigAFont = new Font("Serif", Font.PLAIN, 30);
    private JFrame _window;
    private JList<String> _riders;
    private JList<DeviceEntry> _devices;
    private DefaultListModel<String> _riderModel = new DefaultListModel<>();
    private DefaultListModel<DeviceEntry> _deviceModel = new DefaultListModel<>();
    private JButton _mapButton;
    private HashMap<String, HashMap<PerformanceData.Type, PerformanceData>> _unmappedData = new HashMap<String, HashMap<PerformanceData.Type, PerformanceData>>();
    private HashSet<String> _mappedData = new HashSet<String>();

    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                init();
            }
        });
    }

    private void init() {
        _window = new JFrame();
        _window.setSize(800, 400);
        _window.setTitle("Device Mapper");

        Container content = _window.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));

        renderRiders();

        _devices = new JList<>(_deviceModel);
        content.add(_devices);
        _riders = new JList<>(_riderModel);
        _riders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        content.add(_riders);

        _mapButton = new JButton("Map");
        _mapButton.addActionListener(new MapListener());
        content.add(_mapButton);

        _window.setVisible(true);
    }

    public void handlePerformanceData(final String identifier, final PerformanceData data) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!_unmappedData.containsKey(identifier)) {
                    _unmappedData.put(identifier, new HashMap<PerformanceData.Type, PerformanceData>());
                }
                _unmappedData.get(identifier).put(data.getType(), data);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        renderDevices();
                    }
                });
            }
        });
    }

    private void renderDevices() {
        for (Map.Entry<String, HashMap<PerformanceData.Type, PerformanceData>> entry : _unmappedData.entrySet()) {
            if (_mappedData.contains(entry.getKey())) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (PerformanceData data : entry.getValue().values()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(data.getType() + " " + data.getValue());
            }

            int index = -1;

            for (int i = 0; i < _deviceModel.size(); i++) {
                if (_deviceModel.elementAt(i).getId().equals(entry.getKey())) {
                    index = i;
                    break;
                }
            }

            DeviceEntry de = new DeviceEntry();
            de.setId(entry.getKey());
            de.setText(entry.getKey() + "|" + sb.toString());
            if (index > -1) {
                _deviceModel.set(index, de);
            } else {
                _deviceModel.addElement(de);
            }
        }
    }

    private void renderRiders() {
        _riderModel.clear();
        for (Rider rider : WorkoutSession.instance().getRiders()) {
            _riderModel.addElement(rider.getIdentifier());
        }
    }

    private class MapListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String riderId = _riders.getSelectedValue();
            if (riderId != null && _devices.getSelectedValues() != null) {
                for (Object obj : _devices.getSelectedValues()) {
                    DeviceEntry device = (DeviceEntry) obj;
                    WorkoutSession.instance().associateRider(riderId, device.getId());
                    _mappedData.add(device.getId());
                    _deviceModel.removeElement(device);
                }
            }
        }
    }

    class DeviceEntry {
        private String _id;
        private String _text;

        public String getId() {
            return _id;
        }

        public void setId(String id) {
            _id = id;
        }

        public String getText() {
            return _text;
        }

        public void setText(String text) {
            _text = text;
        }

        @Override
        public String toString() {
            return _text;
        }
    }
}
