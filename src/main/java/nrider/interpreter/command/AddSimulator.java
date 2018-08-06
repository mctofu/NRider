package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.debug.SimController;
import nrider.interpreter.BaseCommand;

public class AddSimulator extends BaseCommand {
    public String getDescription() {
        return "Add a Simulator. - <identifier>";
    }

    public String run(String[] args) {
        if (args.length != 1) {
            return "Invalid syntax";
        }
        SimController simTrainer = new SimController(args[0]);
        try {
            simTrainer.connect();
        } catch (Exception e) {
            return "Error connecting to computrainer: " + e.getMessage();
        }

        WorkoutSession session = WorkoutSession.instance();

        session.addWorkoutController(simTrainer);
        simTrainer.addPerformanceDataListener(session);
        simTrainer.addControlDataListener(session);
        return "Added " + simTrainer.getIdentifier();
    }
}