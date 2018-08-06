package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

public class StartWorkout extends BaseCommand {
    public String getDescription() {
        return "Starts the loaded workout";
    }

    @Override
    public String run(String[] args) {
        WorkoutSession.instance().startRide();
        return null;
    }
}
