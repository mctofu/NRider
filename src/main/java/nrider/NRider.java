package nrider;

import nrider.core.WorkoutSession;

public class NRider {
    public void start(String[] args) {
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
