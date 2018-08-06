package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

public class ControllerDisconnect extends BaseCommand {
    public String getDescription() {
        return "Disconnects all workout controllers";
    }

    public String run(String[] args) {
        WorkoutSession.instance().disconnectControllers();
        return null;
    }
}
