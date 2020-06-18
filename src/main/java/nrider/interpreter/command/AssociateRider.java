package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

public class AssociateRider extends BaseCommand {
    public String getDescription() {
        return "Associate a rider with a device.  <user id> <device id> <ext>";
    }

    public String run(String[] args) {
        if (args.length < 2 || args.length > 3) {
            return "Invalid syntax";
        }

        boolean extMetrics = false;
        if (args.length == 3) {
            if (args[2].equals("ext")) {
                extMetrics = true;
            } else {
                return "Invalid syntax";
            }
        }


        WorkoutSession session = WorkoutSession.instance();

        session.associateRider(args[0], args[1], extMetrics);
        return null;
    }
}
