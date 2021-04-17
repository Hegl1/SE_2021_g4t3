package at.qe.timeguess.dto;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.UserService;

public class UpdateUserDTO {
    private String username;
    private String password;
    private String old_password;
    private String role;

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
