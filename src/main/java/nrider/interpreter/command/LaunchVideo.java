package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.media.MediaEvent;

public class LaunchVideo extends BaseCommand {
    @Override
    public String run(String[] args) {
        MediaEvent meLoad = new MediaEvent(MediaEvent.Type.LOAD, args[0], 0, null);
        WorkoutSession.instance().handleMediaEvent(meLoad);
        if (args.length > 1) {
            MediaEvent meSeek = new MediaEvent(MediaEvent.Type.SEEK, args[0], Integer.parseInt(args[1]), null);
            WorkoutSession.instance().handleMediaEvent(meSeek);
        }

        return null;
    }

    public String getDescription() {
        return "Start a video";
    }
}
