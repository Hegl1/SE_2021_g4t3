package at.qe.timeguess.services;

import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User loadUser(String username) {
        return userRepository.findFirstByUsername(username);
    }

}
