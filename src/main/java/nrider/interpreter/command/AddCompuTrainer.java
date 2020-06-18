package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.io.ComputrainerController;

public class AddCompuTrainer extends BaseCommand {
    public String getDescription() {
        return "Add a CompuTrainer. - <COM PORT NAME>";
    }

    public String run(String[] args) {
        if (args.length < 1 || args.length > 3) {
            return "Invalid syntax";
        }
        ComputrainerController compuTrainer = new ComputrainerController();
        compuTrainer.setCommPortName(args[0]);
        if (args.length > 1) {
            compuTrainer.setIdentifier(args[1]);
        }
        try {
            compuTrainer.connect();
        } catch (Exception e) {
            return "Error connecting to computrainer: " + e.getMessage();
        }

        WorkoutSession session = WorkoutSession.instance();

        session.addWorkoutController(compuTrainer);
        compuTrainer.addPerformanceDataListener(session);
        compuTrainer.addControlDataListener(session);

        if (args.length > 2) {
            session.associateRider(args[2], compuTrainer.getIdentifier(), false);
        }

        return "Added " + compuTrainer.getIdentifier();
    }
}
