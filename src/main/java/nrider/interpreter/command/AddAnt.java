package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.io.ant.AntReceiver;

public class AddAnt extends BaseCommand {
    public String getDescription() {
        return "Add an ANT receiver.  - <COM PORT NAME>";
    }

    @Override
    public String run(String[] args) throws Exception {
        if (args.length != 1) {
            return "Invalid syntax";
        }
        AntReceiver ant = new AntReceiver();
        ant.setCommPortName(args[0]);
        try {
            ant.connect();
        } catch (Exception e) {
            return "Error connecting to ant: " + e.getMessage();
        }

        WorkoutSession session = WorkoutSession.instance();

        session.addPerformanceDataSource(ant);
//		ant.addPerformanceDataListener( session );
//		return "Added " + ant.getIdentifier();
        return "Added ant";
    }
}
