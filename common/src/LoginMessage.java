import java.io.Serializable;

public class LoginMessage implements Serializable {

    private String strongName;
//    private String name;
//    private String pass;
    private boolean newUser;

    //    public LoginMessage(String strongName, String name, String pass, boolean newUser) {
//        this.strongName = strongName;
//        this.name = name;
//        this.pass = pass;
//        this.newUser = newUser;
//    }
    public LoginMessage(String strongName, boolean newUser) {
        this.strongName = strongName;
        this.newUser = newUser;
    }

//    public LoginMessage(boolean needLogin) {
//
//        this.needLogin = needLogin;
//    }

//    public String getName() {
//        return name;
//    }
//
//    public String getPass() {
//        return pass;
//    }

    public boolean isNewUser() {
        return newUser;
    }

    public String getStrongName() {
        return strongName;
    }

    //    public boolean isNeedLogin() {
//        return needLogin;
//    }
}
