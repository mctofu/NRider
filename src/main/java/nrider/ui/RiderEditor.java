package nrider.ui;

import nrider.core.Rider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RiderEditor implements ActionListener {
    private JFrame _window;
    private JTextField _id;
    private JTextField _threshold;
    private final RiderManager _riderManager;

    public RiderEditor(RiderManager riderManager) {
        _riderManager = riderManager;
    }

    public void launch() {
        launch(new Rider());
    }


    public void launch(Rider rider) {
        init(rider);
    }

    private void init(Rider rider) {
        _window = new JFrame();
        _window.setSize(500, 600);

        Container content = _window.getContentPane();
        content.setLayout(new GridLayout(3, 2));

        JLabel idLabel = new JLabel("Id");
        content.add(idLabel);

        _id = new JTextField(rider.getIdentifier());
        content.add(_id);

        JLabel thresholdLabel = new JLabel("Threshold");
        content.add(thresholdLabel);

        _threshold = new JTextField(rider.getThresholdPower());
        content.add(_threshold);

        JButton actionButton = new JButton("Create");
        actionButton.addActionListener(this);
        content.add(actionButton);

        _window.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        Rider rider = new Rider();
        rider.setName(_id.getText());
        rider.setThresholdPower(Integer.parseInt(_threshold.getText()));
        _riderManager.handleCreateRider(rider);
        _window.dispose();
    }
}
