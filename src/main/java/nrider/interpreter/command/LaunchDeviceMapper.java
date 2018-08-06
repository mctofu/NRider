package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.ui.DeviceMapper;

public class LaunchDeviceMapper extends BaseCommand {

    @Override
    public String run(String[] args) throws Exception {
        DeviceMapper dm = new DeviceMapper();

        WorkoutSession.instance().addUnmappedPerformanceDataListener(dm);

        dm.start();

        return null;
    }

    public String getDescription() {
        return "Open device mapper window";
    }
}
