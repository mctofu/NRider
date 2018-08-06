package nrider.media;

public class MediaEvent {
    public enum Type {
        LOAD, PLAY, PAUSE, SEEK, STOP
    }

    private Type _type;
    private String _media;
    private int _position;
    private String _value;

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
