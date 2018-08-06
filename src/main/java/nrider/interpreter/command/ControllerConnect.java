package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

public class ControllerConnect extends BaseCommand {
    public String getDescription() {
        return "Connects workout controllers";
    }

    @Override
    public String run(String[] args) throws Exception {
        WorkoutSession.instance().connectControllers();
        return null;
    }
}
