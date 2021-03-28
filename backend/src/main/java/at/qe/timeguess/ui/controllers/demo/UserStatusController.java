package at.qe.timeguess.ui.controllers.demo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.demo.LogEntry;
import at.qe.timeguess.model.demo.LogEntryType;
import at.qe.timeguess.model.demo.UserStatus;
import at.qe.timeguess.model.demo.UserStatusInfo;
import at.qe.timeguess.repositories.UserRepository;
import at.qe.timeguess.spring.UserStatusInitializationHandler;
import at.qe.timeguess.ui.websockets.WebSocketManager;
import at.qe.timeguess.utils.CDIAutowired;
import at.qe.timeguess.utils.CDIContextRelated;

/**
 * This controller holds and manages all user's status-information (i.e. their
 * online-status)
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 */
@Controller
@Scope("application")
@CDIContextRelated
public class UserStatusController {

    @Autowired
    private UserRepository userRepository;
    @CDIAutowired
    private WebSocketManager websocketManager;
    private Map<String, UserStatusInfo> userStatus = new ConcurrentHashMap<>();
    private List<LogEntry> actionLogs = new CopyOnWriteArrayList<>();

    /**
     * Called by the {@link UserStatusInitializationHandler}: When the
     * database-connection is established, all users can be retrieved and the
     * collection holding the user-status can be setup.
     *
     * NOTE: The {@link UserStatusInitializationHandler} calls this setup-method
     * only once at startup. If you add any other users, they will not appear within
     * the user-status-collection. You either need to restart the application
     * (feasible in development-mode, really bad behavior in production) or call
     * this setup.method again to refresh the mentioned collection
     */
    public void setupUserStatus() {
        this.userRepository.findAll()
                .forEach(user -> this.userStatus.put(user.getUsername(), new UserStatusInfo(user)));
    }

    /**
     * Convenience-method. See
     * {@link #afterStatusChange(String, UserStatus, LogEntryType)}
     *
     * @param username
     */
    public void afterLogin(String username) {
        this.afterStatusChange(username, UserStatus.ONLINE, LogEntryType.USER_LOGIN);
    }

    /**
     * Convenience-method. See
     * {@link #afterStatusChange(String, UserStatus, LogEntryType)}
     *
     * @param username
     */
    public void afterLogout(String username) {
        this.afterStatusChange(username, UserStatus.OFFLINE, LogEntryType.USER_LOGOUT);
    }

    /**
     * After a users logs -in or -out, its status is changed and a corresponding
     * event-log is appended.
     *
     * @param username  The user which performs the action
     * @param newStatus The new user-status
     * @param logType   The log-type for the event to collect
     */
    private void afterStatusChange(String username, UserStatus newStatus, LogEntryType logType) {
        User user = this.userRepository.findFirstByUsername(username);
        // change status
        this.userStatus.get(username).setStatus(newStatus);
        // append log
        this.log(user, logType);
        // notify all users (application-wide)
        this.websocketManager.getUserRegistrationChannel().send("connectionUpdate");
    }

    /**
     * Simply appends a log-entry to the action-log.
     *
     * @param user
     * @param type
     */
    private void log(User user, LogEntryType type) {
        this.actionLogs.add(new LogEntry(user, type));
    }

    public Collection<UserStatusInfo> getUserStatusInfos() {
        return Collections.unmodifiableCollection(this.userStatus.values());
    }

    public List<LogEntry> getActionLogs() {
        return Collections.unmodifiableList(this.actionLogs);
    }

}
