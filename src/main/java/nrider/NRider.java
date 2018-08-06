package nrider;

import nrider.core.WorkoutSession;
import org.apache.log4j.xml.DOMConfigurator;

public class NRider {
    public void start(String[] args) {
        DOMConfigurator.configure("logConfig.xml");
        NRiderClient client = new NRiderClient();
        try {
            client.start();
            WorkoutSession.instance().addPerformanceDataListener(client);
            WorkoutSession.instance().addWorkoutListener(client);
            NRiderConsole console = new NRiderConsole();
            if (args.length > 0) {
                console.runScript(args[0]);
            }

            console.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                WorkoutSession.instance().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        new NRider().start(args);
    }
}
