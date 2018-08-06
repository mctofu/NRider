package nrider;

import nrider.interpreter.CommandInterpreter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NRiderConsole {
    private CommandInterpreter _interpreter = new CommandInterpreter();

    public void runScript(String scriptFilePath) throws IOException {
        System.out.println("Run script " + scriptFilePath);
        BufferedReader reader = new BufferedReader(new FileReader(scriptFilePath));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                processLine(line);
            }
        } finally {
            reader.close();
        }
    }

    public void start() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String input = reader.readLine();
            if ("exit".equals(input)) {
                break;
            }
            processLine(input);
        }
    }

    private void processLine(String input) {
        String output = _interpreter.executeCommand(input);
        if (output != null) {
            System.out.println(output);
        }
    }
}
