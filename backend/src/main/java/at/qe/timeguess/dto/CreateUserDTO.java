package at.qe.timeguess.dto;

import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.UserService;

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
        return UserService.parseRole(this.role);
    }
}
