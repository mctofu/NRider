package nrider.ui;

import nrider.core.Rider;
import nrider.db.FileRiderDb;
import nrider.db.IRiderDb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RiderManager implements ActionListener {
    private JFrame _window;
    private JComboBox<String> _allRiders;
    private final DefaultComboBoxModel<String> _allRiderModel = new DefaultComboBoxModel<>();
    private final IRiderDb _riderDb = new FileRiderDb();

    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                init();
            }
        });
    }

    private void init() {
        _window = new JFrame();
        _window.setSize(500, 600);

        Container content = _window.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel riderDbControls = new JPanel();
        riderDbControls.setLayout(new BoxLayout(riderDbControls, BoxLayout.X_AXIS));

        populateAllRiders();

        _allRiders = new JComboBox(_allRiderModel);
        riderDbControls.add(_allRiders);
//		riderDbControls.setMaximumSize( riderDbControls.getPreferredSize() );

        JButton addToWorkout = new JButton("Add to workout");
        addToWorkout.setActionCommand("addToWorkout");
        addToWorkout.addActionListener(this);
        riderDbControls.add(addToWorkout);
        JButton createRider = new JButton("Create new rider");
        createRider.setActionCommand("createRider");
        createRider.addActionListener(this);
        riderDbControls.add(createRider);

        content.add(riderDbControls);

        _window.setVisible(true);
    }

    private void populateAllRiders() {
        _allRiderModel.removeAllElements();
        for (String riderId : _riderDb.getRiderIds()) {
            _allRiderModel.addElement(riderId);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("addToWorkout")) {

        } else if (e.getActionCommand().equals("createRider")) {
            new RiderEditor(this).launch();
        }
    }

    public void handleCreateRider(Rider rider) {
        _riderDb.addRider(rider);
        populateAllRiders();
    }
}
