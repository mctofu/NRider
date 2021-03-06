package nrider.io;

/**
 * Control message from a workout device related input.
 */
public class ControlData {
    public enum Type {STOP, START, RECALIBRATE, F3, PLUS, MINUS}

    private final Type _type;

    public ControlData(Type type) {
        _type = type;
    }

    public Type getType() {
        return _type;
    }
}
