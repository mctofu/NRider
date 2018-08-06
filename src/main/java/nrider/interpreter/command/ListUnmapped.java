package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ListUnmapped extends BaseCommand {
    public String getDescription() {
        return "List identifiers publishing stats that aren't mapped to riders";
    }

    public String run(String[] args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (String id : WorkoutSession.instance().getUnmappedIdentifiers()) {
            pw.println(id);
        }
        pw.flush();
        return sw.toString();
    }
}
