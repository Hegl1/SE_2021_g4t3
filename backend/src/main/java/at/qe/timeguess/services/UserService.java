package at.qe.timeguess.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.UserRepository;

import java.util.Calendar;

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
        // TODO: implement
        return null;
    }

    public User saveUser(final User user) {
        user.setUpdateDate(Calendar.getInstance().getTime());
        return this.userRepository.save(user);
    }

}
