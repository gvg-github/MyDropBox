import java.io.Serializable;

public class LoginMessage implements Serializable {

    private String strongName;
    private UserActionEnum userActionEnum;

    public LoginMessage(String strongName, UserActionEnum userAction) {
        this.strongName = strongName;
        this.userActionEnum = userAction;
    }

    public String getStrongName() {
        return strongName;
    }

    public UserActionEnum getUserActionEnum() {
        return userActionEnum;
    }
}
