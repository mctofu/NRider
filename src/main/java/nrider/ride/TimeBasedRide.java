package nrider.ride;

import nrider.core.RideLoad;
import nrider.core.WorkoutSession;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimeBasedRide implements IRide {
    private final static Logger LOG = Logger.getLogger(TimeBasedRide.class);

    private Timer _timer;
    private Date _startTime;
    private long _startElapsed;
    private Status _status;
    private final RideScript _timeline;
    private final WorkoutSession _session;

    public TimeBasedRide(WorkoutSession session, RideScript timeline) {
        _session = session;
        _timeline = timeline;
        _status = Status.READY;
    }

    public void start() {
        if (_status == Status.READY || _status == Status.PAUSED) {
            LOG.info("Starting ride");
            _timer = new Timer("TimeBasedRide", true);
            _startTime = new Date();
            if (_status == Status.READY) {
                _startElapsed = 0;
            }
            _status = Status.RUNNING;
            _timer.scheduleAtFixedRate(new TimeUpdateTask(), 0, 250);
            LOG.info("Started ride");
        }
    }

    public void pause() {
        if (_status == Status.RUNNING) {
            _timer.cancel();
            _startElapsed += new Date().getTime() - _startTime.getTime();
            _status = Status.PAUSED;
            LOG.info("Paused ride");
        }
    }

    public void stop() {
        pause();
        _status = Status.STOPPED;
        LOG.info("Stopped ride");
    }

    public Status getStatus() {
        return _status;
    }

    public RideScript getScript() {
        return _timeline;
    }

    public class TimeUpdateTask extends TimerTask {
        public void run() {
            long elapsed = _startElapsed + new Date().getTime() - _startTime.getTime();

            _session.setRideElapsedTime(elapsed);

            RideLoad load = _timeline.getLoad(elapsed);
            if (load != null) {
                _session.setRideLoad(load);
            }
        }
    }
}
