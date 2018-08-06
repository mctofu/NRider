package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.net.NRiderServer;

import java.io.IOException;

public class StartServer extends BaseCommand {
    public String getDescription() {
        return "Start a server for other NRider clients to connect to over the network. <port number>";
    }

    public String run(String[] args) {
        if (args.length != 1) {
            return "Invalid usage";
        }
        try {
            NRiderServer server = new NRiderServer(Integer.parseInt(args[0]));
            server.start();
            WorkoutSession.instance().setNetSource(server);
        } catch (IOException e) {
            return "Unable to start server: " + e.getMessage();
        }

        return null;
    }

}
