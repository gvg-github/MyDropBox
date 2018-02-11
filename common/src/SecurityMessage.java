import javax.crypto.SecretKey;
import java.io.Serializable;

public class SecurityMessage implements Serializable{
    private SecretKey key;

    public SecurityMessage(SecretKey key) {
        this.key = key;
    }

    public SecurityMessage() {

    }

    public SecretKey getKey() {
        return key;
    }
}
