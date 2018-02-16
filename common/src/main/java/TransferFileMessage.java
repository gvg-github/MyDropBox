import java.io.Serializable;

public class TransferFileMessage implements Serializable {
    private String name;
    private String path;
    private byte[] data;
    private boolean endOfFile;
    private boolean transfer;
    private long size;

    public TransferFileMessage(String name, String path, long size, byte[] data, boolean transfer, boolean endOfFile) {
        this.name = name;
        this.path = path;
        this.data = data;
        this.endOfFile = endOfFile;
        this.transfer = transfer;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isEndOfFile() {
        return endOfFile;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public long getSize() {
        return size;
    }
}
