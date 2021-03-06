package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.ride.IRide;
import nrider.ride.RideLoader;

public class WorkoutLoad extends BaseCommand {
    public String getDescription() {
        return "Loads a workout file.  <filename>";
    }

    @Override
    public String run(String[] args) throws Exception {
        IRide ride = new RideLoader().loadRide(args[0]);
        if (args.length > 1) {
            ride.getScript().adjustLoad(Double.parseDouble(args[1]));
        }

        if (args.length > 4) {
            ride.getScript().crop(Long.parseLong(args[3]), Long.parseLong(args[4]));
        }

        if (args.length > 2) {
            ride.getScript().adjustLength(Double.parseDouble(args[2]));
        }

        WorkoutSession.instance().setRide(ride);
        return null;
    }
}
