package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.io.SportGrpcController;

public class AddSportGrpc extends BaseCommand
{
    public String getDescription()
    {
        return "Add a sport grpc controller - <host:port> <name>";
    }

    public String run( String[] args ) {
        if (args.length < 1 || args.length > 2) {
            return "Invalid syntax";
        }

        WorkoutSession session = WorkoutSession.instance();

        SportGrpcController sportGrpc = new SportGrpcController(args[0], args[1]);
        sportGrpc.addWorkoutControllerDiscoveryListener(session::addWorkoutController);

        try {
            sportGrpc.connect();
        } catch(Exception e) {
            return "Error connecting to remote controller: " + e.getMessage();
        }

        session.addResourceToCleanup(sportGrpc);
        sportGrpc.addPerformanceDataListener(session);
        sportGrpc.addControlDataListener(session);

        return "Added " + sportGrpc.getIdentifier();
    }
}
