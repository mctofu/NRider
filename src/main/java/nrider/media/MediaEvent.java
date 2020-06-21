package nrider.media;

public class MediaEvent {
    public enum Type {
        LOAD, PLAY, PAUSE, SEEK, STOP
    }

    private final Type _type;
    private final String _media;
    private final int _position;
    private final String _value;

    public MediaEvent(Type type, String media, int position, String value) {
        _type = type;
        _media = media;
        _position = position;
        _value = value;
    }

    public Type getType() {
        return _type;
    }

    public String getMedia() {
        return _media;
    }

    public int getPosition() {
        return _position;
    }

    public String getValue() {
        return _value;
    }
}
