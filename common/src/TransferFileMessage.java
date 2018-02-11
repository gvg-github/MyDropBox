import java.io.Serializable;

public class TransferFileMessage implements Serializable {
    private String name;
    private byte[] data;
    private boolean endOfFile;
    private boolean transfer;
    private long size;

    public TransferFileMessage(String name, long size, byte[] data, boolean transfer, boolean endOfFile) {
        this.name = name;
        this.data = data;
        this.endOfFile = endOfFile;
        this.transfer = transfer;
        this.size = size;
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
