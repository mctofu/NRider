package nrider.interpreter.command;

import nrider.interpreter.BaseCommand;
import nrider.ui.RiderManager;

public class LaunchRiderManager extends BaseCommand {
    @Override
    public String run(String[] args) throws Exception {
        RiderManager rm = new RiderManager();

//		WorkoutSession.instance().addUnmappedPerformanceDataListener( rm );

        rm.start();

        return null;
    }

    public String getDescription() {
        return "Open rider manager window";
    }
}
