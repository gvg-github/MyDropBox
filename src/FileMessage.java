import java.io.Serializable;

class FileMessage implements Serializable {
    private String name;
    private byte[] data;

    public FileMessage(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
}
