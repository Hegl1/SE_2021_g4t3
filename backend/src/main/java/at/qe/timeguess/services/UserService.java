package at.qe.timeguess.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public User loadUser(final String username) {
		return userRepository.findFirstByUsername(username);
	}

	public User getAuthenticatedUser() {
		// TODO: implement
		return null;
	}

}
