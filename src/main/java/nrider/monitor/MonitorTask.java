package nrider.monitor;

import java.util.ArrayList;
import java.util.TimerTask;

public class MonitorTask extends TimerTask {
    private ArrayList<IMonitorable> _targets = new ArrayList<IMonitorable>();

    @Override
    public void run() {
        synchronized (_targets) {
            for (IMonitorable m : _targets) {
                m.monitorCheck();
            }
        }
    }

    public void addMonitorable(IMonitorable m) {
        synchronized (_targets) {
            _targets.add(m);
        }
    }

    public void removeMonitorable(IMonitorable m) {
        synchronized (_targets) {
            _targets.remove(m);
        }
    }
}

