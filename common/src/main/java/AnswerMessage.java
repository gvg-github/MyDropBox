import java.io.File;
import java.io.Serializable;

public class AnswerMessage implements Serializable {
    private boolean yes;
    private String msg;
    private File[] files;
    private int size;

    public AnswerMessage(boolean yes, String msg, File[] files, int size) {

        this.yes = yes;
        this.msg = msg;
        this.files = files;
        this.size = size;
    }

    public boolean isYes() {
        return yes;
    }

    public String getMsg() {
        return msg;
    }

    public File[] getFiles() {
        return files;
    }

    public int getSize() {
        return size;
    }
}
