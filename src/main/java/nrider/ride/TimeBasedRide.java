package nrider.ride;

import nrider.core.RideLoad;
import nrider.core.WorkoutSession;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimeBasedRide implements IRide {
    private Timer _timer;
    private Date _startTime;
    private int _elapsed;
    private Status _status;
    private final RideScript _timeline;

    public TimeBasedRide(RideScript timeline) {
        _timeline = timeline;
        _status = Status.READY;
    }

    public void start() {
        if (_status == Status.READY || _status == Status.PAUSED) {
            _timer = new Timer("TimeBasedRide", true);
            _startTime = new Date();
            if (_status == Status.READY) {
                _elapsed = 0;
            }
            _status = Status.RUNNING;
            for (RideEvent te : _timeline) {
                if (te.getPosition() >= _elapsed) {
                    _timer.schedule(new LoadTask(te.getLoad()), te.getPosition() - _elapsed);
                }
            }
            _timer.scheduleAtFixedRate(new TimeUpdateTask(), 500, 500);
        }
    }

    public void pause() {
        if (_status == Status.RUNNING) {
            _timer.cancel();
            _elapsed += new Date().getTime() - _startTime.getTime();
            _status = Status.PAUSED;
        }
    }

    public void stop() {
        pause();
        _status = Status.STOPPED;
    }

    public Status getStatus() {
        return _status;
    }

    public RideScript getScript() {
        return _timeline;
    }

    public class TimeUpdateTask extends TimerTask {
        public void run() {
            long elapsed = _elapsed + new Date().getTime() - _startTime.getTime();
            WorkoutSession.instance().setRideElapsedTime(elapsed);
        }
    }

    public class CompleteTask extends TimerTask {
        public void run() {
            WorkoutSession session = WorkoutSession.instance();

            session.setRideLoad(new RideLoad(RideLoad.Type.WATTS, 50));
        }
    }

    public class LoadTask extends TimerTask {
        private final RideLoad _load;

        public LoadTask(RideLoad load) {
            _load = load;
        }

        public void run() {
            try {
                WorkoutSession session = WorkoutSession.instance();
                session.setRideLoad(_load);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
