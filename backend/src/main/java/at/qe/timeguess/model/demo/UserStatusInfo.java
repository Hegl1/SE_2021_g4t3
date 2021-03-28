package at.qe.timeguess.model.demo;

import at.qe.timeguess.model.User;

/**
 * Just combines a user and its status.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 */
public class UserStatusInfo {

    private User user;
    private UserStatus status = UserStatus.OFFLINE;

    public UserStatusInfo(User user) {
            super();
            this.user = user;
    }

    public User getUser() {
            return user;
    }

    public void setUser(User user) {
            this.user = user;
    }

    public UserStatus getStatus() {
            return status;
    }

    public void setStatus(UserStatus status) {
            this.status = status;
    }

}
