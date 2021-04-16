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

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return userRepository.findFirstByUsername(auth.getName());
        }

        return null;
    }

    public User saveUser(final User user) throws UsernameNotAvailableException {
        User existingUser = this.userRepository.findFirstByUsername(user.getUsername());
        if (existingUser != null && existingUser.getId() != user.getId()) {
            throw new UsernameNotAvailableException("User can't be saved because another user with same username already exists!");
        }
        if (existingUser == null || !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setUpdateDate(new Date(System.currentTimeMillis()));
        return this.userRepository.save(user);

    }

    public void deleteUser(final User user) {
        for (CompletedGameTeam completedGameTeam : completedGameTeamRepository.findByUser(user)) {
            List<User> playerList = completedGameTeam.getPlayers();
            playerList.set(playerList.indexOf(user),null);
            this.completedGameTeamRepository.save(completedGameTeam);
        }

        this.userRepository.delete(user);
    }

    public class UsernameNotAvailableException extends Exception {

        private static final long serialVersionUID = 1L;

        public UsernameNotAvailableException(final String message) {
            super(message);
        }
    }

}
