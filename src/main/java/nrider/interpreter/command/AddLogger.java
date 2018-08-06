package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.datalog.RideLogger;
import nrider.interpreter.BaseCommand;

public class AddLogger extends BaseCommand {
    public String getDescription() {
        return "Add a data logger";
    }

    public String run(String[] args) {
        RideLogger rideLogger = new RideLogger();
        WorkoutSession.instance().addPerformanceDataListener(rideLogger);
        WorkoutSession.instance().addWorkoutListener(rideLogger);
        return null;
    }
}