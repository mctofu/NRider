package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * list identifiers mapped to riders
 */
public class ListMapped extends BaseCommand {
    @Override
    public String run(String[] args) throws Exception {
        StringWriter output = new StringWriter();
        PrintWriter outputWriter = new PrintWriter(output);

        for (Map.Entry<String, String> entry : WorkoutSession.instance().getMappedIdentifiers().entrySet()) {
            outputWriter.println(entry.getKey() + "\t" + entry.getValue());
        }

        return output.toString();
    }

    public String getDescription() {
        return "List identifiers mapped to riders";
    }
}
