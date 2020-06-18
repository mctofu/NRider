package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.io.RemoteController;

public class AddRemote extends BaseCommand
{
    public String getDescription()
    {
        return "Add a remote controller - <host:port> <name>";
    }

    public String run( String[] args ) {
        if (args.length < 1 || args.length > 3) {
            return "Invalid syntax";
        }
        RemoteController remote = new RemoteController(args[0], args[1]);
        try {
            remote.connect();
        } catch(Exception e) {
            return "Error connecting to remote controller: " + e.getMessage();
        }

        WorkoutSession session = WorkoutSession.instance();

        session.addWorkoutController(remote);
        remote.addPerformanceDataListener(session);
        remote.addControlDataListener(session);

        if (args.length > 2)
        {
            session.associateRider(args[2], remote.getIdentifier(), false);
        }

        return "Added " + remote.getIdentifier();
    }
}
