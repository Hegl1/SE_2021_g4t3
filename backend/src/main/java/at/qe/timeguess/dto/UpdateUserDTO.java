package at.qe.timeguess.dto;

import at.qe.timeguess.model.UserRole;

public class UpdateUserDTO {
    private String username;
    private String password;
    private String old_password;
    private String role;

    public String getUsername() {
        return username;
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

    public String getPassword() {
        return password;
    }

    public String getOld_password() {
        return old_password;
    }
}
