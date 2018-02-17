import java.io.Serializable;

public class FileMessage implements Serializable {
    private String name;
    private Enum action;
    private String newName;
    private String tecPath;

    public FileMessage(String name, FileActionEnum action, String newName, String tecPath) {
        this.name = name;
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



    public String getNewName() {
        return newName;
    }

    public String getTecPath() {
        return tecPath;
    }

}
