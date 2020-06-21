package nrider.interpreter;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Help extends BaseCommand {
    public final CommandInterpreter _commandInterpreter;

    public Help(CommandInterpreter ci) {
        _commandInterpreter = ci;
    }

    public String getDescription() {
        return "Lists available commands";
    }

    public String run(String[] args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        for (ICommand command : _commandInterpreter.getCommands()) {
            pw.println(command.getName() + ": " + command.getDescription());
        }
        pw.println("exit: Quit the application");
        pw.flush();
        return sw.toString();
    }
}
