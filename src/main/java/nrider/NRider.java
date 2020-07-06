package nrider;

import nrider.core.WorkoutSession;

import java.io.InputStream;

public class NRider {
    public void start(String[] args) {
        InputStream in = System.in;

        NRiderConsole console = new NRiderConsole(in);

        NRiderClient client = new NRiderClient(console::cancel);

        try {
            client.start();
            WorkoutSession.instance().addPerformanceDataListener(client);
            WorkoutSession.instance().addWorkoutListener(client);

            if (args.length > 0) {
                console.runScript(args[0]);
            }

            console.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                WorkoutSession.instance().close();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        new NRider().start(args);
    }
}
