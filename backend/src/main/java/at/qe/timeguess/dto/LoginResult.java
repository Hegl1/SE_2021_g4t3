package at.qe.timeguess.dto;

import at.qe.timeguess.model.User;

/**
 * Class that gets returned upon successful login and registration of a user.
 * Returns the authenticated/registered user and a token which is needed for authorization in other requests.
 */
public class LoginResult {

    private User user;
    private String token;

    public LoginResult(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
