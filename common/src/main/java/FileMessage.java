import java.io.Serializable;

public class FileMessage implements Serializable {
    private String name;
//    private byte[] data;
    private Enum action;
    private String newName;
    private String tecPath;

    public FileMessage(String name, FileActionEnum action, String newName, String tecPath) {
        this.name = name;
//        this.data = data;
        this.action = action;
        this.newName = newName;
        this.tecPath = tecPath;
    }

    public Enum getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

//    public byte[] getData() {
//        return data;
//    }

    public String getNewName() {
        return newName;
    }

    public String getTecPath() {
        return tecPath;
    }

}
