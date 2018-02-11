import java.io.Serializable;

public class FolderMessage implements Serializable {
    private String name;
    private String newName;
    private boolean delete;
    private boolean create;

    public FolderMessage(String name, boolean create, boolean delete, String newName) {
        this.name = name;
        this.newName = newName;
        this.delete = delete;
        this.create = create;
    }

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public boolean isDelete() {
        return delete;
    }

    public boolean isCreate() {
        return create;
    }
}
