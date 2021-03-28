package at.qe.timeguess.dto;

import at.qe.timeguess.model.User;

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
