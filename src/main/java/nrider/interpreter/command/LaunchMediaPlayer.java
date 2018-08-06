package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.ui.MediaPlayerView;

public class LaunchMediaPlayer extends BaseCommand {
    @Override
    public String run(String[] args) throws Exception {
        MediaPlayerView mpv = new MediaPlayerView();

        String vlcPath = args.length > 0 ? args[0] : null;
        mpv.launch(vlcPath);

        WorkoutSession.instance().addMediaEventListner(mpv);
        WorkoutSession.instance().addWorkoutListener(mpv);

        return null;
    }

    public String getDescription() {
        return "Open media player window";
    }
}
