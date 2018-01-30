import java.io.Serializable;

class LoginMessage implements Serializable {

    private String name;
    private String pass;
    private boolean newUser;

    public LoginMessage(String name, String pass, boolean newUser) {
        this.name = name;
        this.pass = pass;
        this.newUser = newUser;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public boolean getNewUser(){
        return newUser;
    }
}
