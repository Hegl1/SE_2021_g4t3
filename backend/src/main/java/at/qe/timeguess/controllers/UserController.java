package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.Login;
import at.qe.timeguess.dto.LoginResult;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/users")
@RestController
public class UserController {

    static final long TOKEN_EXPIRES = 1000 * 5 * 24 * 60 * 60L;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody Login login) {

        User retrievedUser = userService.loadUser(login.getUsername());

        String receivedPassword = login.getPassword();

        if (retrievedUser != null && receivedPassword != null) {
            if (passwordEncoder.matches(receivedPassword, retrievedUser.getPassword())) {
                String token = authenticationService.generateToken(retrievedUser, UserController.TOKEN_EXPIRES);
                return new ResponseEntity<>(new LoginResult(retrievedUser,token),HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

}
