package nrider.interpreter.command;

import nrider.interpreter.BaseCommand;
import nrider.io.SerialDevice;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ListPorts extends BaseCommand {
    public String getDescription() {
        return "List COM ports";
    }

    public String run(String[] args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (String id : SerialDevice.getPortIdentifiers()) {
            pw.println(id);
        }
        return sw.toString();
    }
}
