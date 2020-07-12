package nrider.ui;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Hack to keep the display from going to sleep.
 */
public class ScreenActiveTask implements ActionListener {
    private final Robot _robot;

    public ScreenActiveTask() {
        try {
            _robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PointerInfo pointer = MouseInfo.getPointerInfo();
        if (pointer == null) {
            // no mouse!
            return;
        }

        Point origLocation = pointer.getLocation();
        int newX = origLocation.x > 0 ? origLocation.x - 1 : origLocation.x + 1;
        int newY = origLocation.y > 0 ? origLocation.y - 1 : origLocation.y + 1;
        _robot.mouseMove(newX, newY);
        _robot.mouseMove(origLocation.x, origLocation.y);
    }
}
