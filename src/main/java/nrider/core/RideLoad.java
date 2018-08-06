package nrider.core;

import java.text.NumberFormat;

public class RideLoad {
    private static NumberFormat INT_FORMAT = NumberFormat.getIntegerInstance();

    public enum Type {
        PERCENT_THRESHOLD {
            public String format(double value) {
                return INT_FORMAT.format(value) + "%";
            }
        },
        WATTS {
            public String format(double value) {
                return Double.toString(value);
            }
        },
        GRADIENT {
            public String format(double value) {
                return value + "%";
            }
        };

        public abstract String format(double value);
    }

    private Type _type;
    private double _value;

    public RideLoad(Type type, double value) {
        _type = type;
        _value = value;
    }

    public Type getType() {
        return _type;
    }

    public double getValue() {
        return _value;
    }

    public void setValue(double value) {
        _value = value;
    }

    @Override
    public String toString() {
        return _type.format(_value);
    }
}
