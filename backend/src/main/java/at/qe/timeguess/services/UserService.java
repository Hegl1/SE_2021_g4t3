package at.qe.timeguess.services;

import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.repositories.CompletedGameTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.UserRepository;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompletedGameTeamRepository completedGameTeamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User getUserById(final Long id) {
        return userRepository.findFirstById(id);
    }

    public User getUserByUsername(final String username) {
        return userRepository.findFirstByUsername(username);
    }

    /**
     * Returns the authenticated user of the current request.
     *
     * @return null if no user authenticated otherwise the authenticated user of the request
     */
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return userRepository.findFirstByUsername(auth.getName());
        }

        return null;
    }

    /**
     * If existing user was edited it saves the new values of the user otherwise it creates a new user.
     * The method checks if the password has been changed or is new because if that is the case it needs to be
     * encrypted before the user gets saved.
     *
     * @param user
     * @return null if no user could be saved otherwise the saved user
     * @throws UsernameNotAvailableException thrown if other user with same username already exists
     */
    public User saveUser(final User user) throws UsernameNotAvailableException, EmptyPasswordException {
        User existingUser = this.userRepository.findFirstByUsername(user.getUsername());
        if (existingUser != null && existingUser.getId() != user.getId()) {
            throw new UsernameNotAvailableException("User can't be saved because another user with same username already exists!");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new EmptyPasswordException("User can't be saved because given password is empty!");
        }

        if (existingUser == null || !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setUpdateDate(new Date(System.currentTimeMillis()));
        return this.userRepository.save(user);

    }

    /**
     * Removes all references from the user to completedGameTeams and deletes the user afterwards.
     *
     * @param user
     */
    public void deleteUser(final User user) {
        for (CompletedGameTeam completedGameTeam : completedGameTeamRepository.findByUser(user)) {
            List<User> playerList = completedGameTeam.getPlayers();
            playerList.set(playerList.indexOf(user), null);
            this.completedGameTeamRepository.save(completedGameTeam);
        }

        this.userRepository.delete(user);
    }

    /**
     * Returns all users where the username contains the searchstring.
     * @param searchString
     * @return List of users
     */
    public List<User> searchUsers(String searchString) {
        return userRepository.searchByUsername(searchString);
    }

    public class UsernameNotAvailableException extends Exception {

        private static final long serialVersionUID = 1L;

        public UsernameNotAvailableException(final String message) {
            super(message);
        }
    }

    public class EmptyPasswordException extends Exception {

        private static final long serialVersionUID = 1L;

        public EmptyPasswordException(final String message) {
            super(message);
        }
    }


}
