package at.qe.timeguess.dto;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.UserService;

/**
 * Class used to send updated values of a user. Not possible because entity does not have
 * a field for the old password. This is needed for security reasons.
 */
public class UpdateUserDTO {
    private String username;
    private String password;
    private String old_password;
    private String role;

    public UpdateUserDTO(String username, String password, String old_password, String role) {
        this.username = username;
        this.password = password;
        this.old_password = old_password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return UserService.parseRole(this.role);
    }

    public boolean hasChanges(User user) {
        return (this.username != null && !user.getUsername().equals(this.username))
            || (this.role != null && !user.getRole().equals(this.role))
            || this.password != null;
    }

    public String getPassword() {
        return password;
    }

    public String getOld_password() {
        return old_password;
    }
}
