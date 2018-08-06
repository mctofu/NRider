package nrider.io.ant;

public class AntData {

    private int _channelId;
    private int _dataLength;
    private int _messageId;
    private byte[] _data;

    AntData(byte[] message) {
        _channelId = (int) message[3] & 0xFF;
        _dataLength = (int) message[1] & 0xFF;
        _messageId = (int) message[2] & 0xFF;
        _data = new byte[_dataLength];
        System.arraycopy(message, 4, _data, 0, _dataLength);
    }

    public int getChannelId() {
        return _channelId;
    }

    public int getDataLength() {
        return _dataLength;
    }

    public int getMessageId() {
        return _messageId;
    }

    public byte[] getData() {
        return _data;
    }
}