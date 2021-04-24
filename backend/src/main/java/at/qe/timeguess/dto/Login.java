package at.qe.timeguess.dto;

/**
 * Class used to send username and password when registering a new user or when logging in into an account.
 * Needed because for a login or registering there is no role field needed.
 */
public class Login {
    private String username;
    private String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
