package at.qe.timeguess.dto;

import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.UserService;

/**
 * Class used to send user data when creating a user.
 * Needed because entities should not be send on post requests for security reasons.
 */
public class CreateUserDTO {
    private String username;
    private String password;
    private String role;

    public CreateUserDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

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
