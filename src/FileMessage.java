import java.io.Serializable;

public class FileMessage implements Serializable {
    private String name;
    private byte[] data;
    private boolean delete;
    private String newName;
    private String tecPath;
    private boolean refresh;

    public FileMessage(String name, byte[] data, boolean delete, boolean refresh, String newName, String tecPath) {
        this.name = name;
        this.data = data;
        this.delete = delete;
        this.newName = newName;
        this.tecPath = tecPath;
        this.refresh = refresh;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isDelete() {
        return delete;
    }

    public String getNewName() {
        return newName;
    }

    public String getTecPath() {
        return tecPath;
    }

    public boolean isRefresh() {
        return refresh;
    }
}
