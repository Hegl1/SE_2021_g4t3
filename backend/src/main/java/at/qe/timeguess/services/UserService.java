package at.qe.timeguess.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.UserRepository;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

        user.setUpdateDate(new Date(System.currentTimeMillis()));
        return this.userRepository.save(user);

    }

    public class UsernameNotAvailableException extends Exception {

        private static final long serialVersionUID = 1L;

        public UsernameNotAvailableException(final String message) {
            super(message);
        }
    }

}
