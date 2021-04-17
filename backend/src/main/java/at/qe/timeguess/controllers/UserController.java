package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.Login;
import at.qe.timeguess.dto.LoginResult;
import at.qe.timeguess.dto.CreateUserDTO;
import at.qe.timeguess.dto.UpdateUserDTO;
import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody Login login) {

        User retrievedUser = userService.getUserByUsername(login.getUsername());

        String receivedPassword = login.getPassword();

        if (retrievedUser != null && receivedPassword != null) {
            if (passwordEncoder.matches(receivedPassword, retrievedUser.getPassword())) {
                String token = authenticationService.generateTokenWithFixedExpiration(retrievedUser);
                return new ResponseEntity<>(new LoginResult(retrievedUser, token), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResult> register(@RequestBody Login login) {
        String username = login.getUsername();
        String password = login.getPassword();
        if (username != null && password != null) {

            try {
                User createdUser = userService.saveUser(new User(username, password, UserRole.PLAYER));
                String token = authenticationService.generateTokenWithFixedExpiration(createdUser);
                return new ResponseEntity<>(new LoginResult(createdUser, token), HttpStatus.OK);
            } catch (UserService.UsernameNotAvailableException e) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT);
            } catch (UserService.EmptyPasswordException e2) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putUser(@PathVariable Long id, @RequestBody UpdateUserDTO updateUserDTO) {
        User user = this.userService.getUserById(id);
        String username = updateUserDTO.getUsername();
        String password = updateUserDTO.getPassword();
        String oldPassword = updateUserDTO.getOld_password();
        UserRole role = updateUserDTO.getRole();

        User authorizedUser = this.userService.getAuthenticatedUser();

        if (user == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        //if no changes in user do not update
        if (!updateUserDTO.hasChanges(user) || passwordEncoder.matches(password, user.getPassword())) {
            return new ResponseEntity(HttpStatus.OK);
        }

        if (username != null) {
            user.setUsername(username);
        }

        if (role != null && !user.getRole().equals(role)) {
            //user can not change it's own role only admins can changes roles
            if (authorizedUser.getRole().equals(UserRole.ADMIN)) {
                user.setRole(role);
            } else {
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }
        }

        //admin does not need to send old password
        if (authorizedUser.getRole().equals(UserRole.ADMIN)) {
            user.setPassword(password);
        } else {
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(password);
            } else {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        }

        try {
            userService.saveUser(user);
        } catch (UserService.UsernameNotAvailableException e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (UserService.EmptyPasswordException e2) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable Long id) {
        User userToDelete = this.userService.getUserById(id);
        if (userToDelete != null) {
            this.userService.deleteUser(userToDelete);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping
    public List<User> getUsers() {
        return this.userService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserDTO createUserDTO) {
        String username = createUserDTO.getUsername();
        String password = createUserDTO.getPassword();
        UserRole role = createUserDTO.getRole();
        if (username != null && password != null && role != null) {

            try {
                User createdUser = userService.saveUser(new User(username, password, role));
                return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
            } catch (UserService.UsernameNotAvailableException e) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT);
            } catch (UserService.EmptyPasswordException e2) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }


    @GetMapping("search/{username}")
    public List<User> searchUsers(@PathVariable String username) {
        return this.userService.searchUsers(username);
    }

}
