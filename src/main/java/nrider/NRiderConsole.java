package nrider;

import nrider.interpreter.CommandInterpreter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NRiderConsole {
    private final CommandInterpreter _interpreter = new CommandInterpreter();
    private final Object _lock = new Object();
    private final InputStream _in;

    private boolean _cancelled;

    public NRiderConsole(InputStream in) {
        _in = in;
    }

    public void runScript(String scriptFilePath) throws IOException {
        System.out.println("Run script " + scriptFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFilePath))) {
            reader.lines().forEach(line -> {
                System.out.println(line);
                processLine(line);
            });
        }
    }

    public void start() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(_in));

        while (true) {
            if (reader.ready()) {
                String input = reader.readLine();
                if ("exit".equals(input) || input == null) {
                    return;
                }
                processLine(input);
            } else {
                synchronized (_lock) {
                    if (_cancelled) {
                        return;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public void cancel() {
        synchronized (_lock) {
            _cancelled = true;
        }
    }

    private void processLine(String input) {
        String output = _interpreter.executeCommand(input);
        if (output != null) {
            System.out.println(output);
        }
    }
}
