package at.qe.timeguess.dto;

import at.qe.timeguess.model.UserRole;

public class CreateUserDTO {
    private String username;
    private String password;
    private String role;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        switch(role) {
            case "admin":
                return UserRole.ADMIN;
            case "gamemanager":
                return UserRole.GAMEMANAGER;
            case "player":
                return UserRole.PLAYER;
        }
        return null;
    }
}
