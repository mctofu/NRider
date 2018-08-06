package nrider.interpreter.command;

import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

public class AddNetRider extends BaseCommand {
    public String getDescription() {
        return "Add a network rider to the workout: <name> <threshold power>";
    }

    public String run(String[] args) {
        if (args.length != 2) {
            return "Invalid usage";
        }
        Rider rider = new Rider();
        rider.setName(args[0]);
        rider.setThresholdPower(Integer.parseInt(args[1]));
        WorkoutSession.instance().addNetRider(rider, "manual");
        return null;
    }
}
