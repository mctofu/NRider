package nrider.interpreter.command;

import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ListRiders extends BaseCommand {
    public String getDescription() {
        return "List riders in the workout";
    }

    public String run(String[] args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (Rider r : WorkoutSession.instance().getRiders()) {
            pw.println(r.getName() + " " + r.getThresholdPower());
        }
        pw.flush();
        return sw.toString();
    }
}
