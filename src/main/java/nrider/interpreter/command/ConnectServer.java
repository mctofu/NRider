package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.net.NRiderSlave;

import java.io.IOException;

public class ConnectServer extends BaseCommand {
    public String getDescription() {
        return "Connect to a running NRider server. <ipaddress> <port number>";
    }

    public String run(String[] args) {
        if (args.length != 2) {
            return "Invalid usage";
        }
        try {
            NRiderSlave client = new NRiderSlave(args[0], Integer.parseInt(args[1]));
            client.connect();
            WorkoutSession.instance().setNetSource(client);
        } catch (IOException e) {
            return "Unable to start server: " + e.getMessage();
        }

        return null;
    }
}
