package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.CreateUserDTO;
import at.qe.timeguess.dto.Login;
import at.qe.timeguess.dto.LoginResult;
import at.qe.timeguess.dto.UpdateUserDTO;
import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class that handles the user endpoint of the API. It takes care of the authentication and registration of users and
 * handles creating, modifying, searching and deleting users.
 */
@RequestMapping("/users")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LobbyService lobbyService;

    /**
     * Method that returns a JWT Token needed for authorization upon successful login.
     *
     * @param login DTO that contains username and password
     * @return ResponseEntity: Status 401 when wrong credentials are entered, Status 200 with LoginResult DTO when
     * login was successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {

        User retrievedUser = userService.getUserByUsername(login.getUsername());

        String receivedPassword = login.getPassword();

        if (retrievedUser != null && receivedPassword != null) {
            if (passwordEncoder.matches(receivedPassword, retrievedUser.getPassword())) {
                String token = authenticationService.generateTokenWithFixedExpiration(retrievedUser);
                return new ResponseEntity<>(new LoginResult(retrievedUser, token), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>("Username or password are wrong", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Method that registers a new user and assigns the role player to it
     *
     * @param login DTO that contains username and password
     * @return ResponseEntity: 201 if user could be created, 409 if a user with the same username already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Login login) {
        String username = login.getUsername();
        String password = login.getPassword();
        if (username != null && password != null) {

            try {
                User createdUser = userService.saveUser(new User(username, password, UserRole.PLAYER));
                String token = authenticationService.generateTokenWithFixedExpiration(createdUser);
                return new ResponseEntity<>(new LoginResult(createdUser, token), HttpStatus.CREATED);
            } catch (UserService.UsernameNotAvailableException e) {
                return new ResponseEntity<>("The username was already taken", HttpStatus.CONFLICT);
            } catch (UserService.EmptyPasswordException e2) {
                return new ResponseEntity<>("Sent password is empty", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Username and Password required", HttpStatus.BAD_REQUEST);
    }

    /**
     * Returns the user with the given id.
     *
     * @param id number that represents the id of a user
     * @return 200 and User entity without password and timestamp on success, 404 when user couldn't be found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Method that changes a user if the authenticated user has the right permissions to modify a user and
     * modify it's role. Only admins can change other users than themselves and are to only ones allowed to change the role of a user.
     * A non admin user can only change his password and username and needs to verify himself with his current password.
     *
     * @param id            number that represents the id of a user
     * @param updateUserDTO DTO that contains the changed values of the user and its current password
     * @ return ResponseEntity: 200 when user could be changed successfully, 400 wrong old password or empty password was given
     * 403 when the user hadn't the permissions to change a user, 404 user with specified user id doesn't
     * exist, 409 if new username is the same as a username of an existing user or user is in game
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> putUser(@PathVariable Long id, @RequestBody UpdateUserDTO updateUserDTO) {
        User user = this.userService.getUserById(id);
        String username = updateUserDTO.getUsername();
        String password = updateUserDTO.getPassword();
        String oldPassword = updateUserDTO.getOld_password();
        UserRole role = updateUserDTO.getRole();

        User authorizedUser = this.userService.getAuthenticatedUser();

        if (authorizedUser == null) {
            return new ResponseEntity("There was no authentication provided or it was invalid", HttpStatus.UNAUTHORIZED);
        }

        boolean isAdmin = authorizedUser.getRole().equals(UserRole.ADMIN);

        if (user == null) {
            return new ResponseEntity("Not Found", HttpStatus.NOT_FOUND);
        }

        if (lobbyService.isUserInGame(user)) {
            return new ResponseEntity("User cannot be deleted because he is currently in a game.", HttpStatus.CONFLICT);
        }

        //if no changes in user do not update
        if (!updateUserDTO.hasChanges(user)) {
            return new ResponseEntity(HttpStatus.OK);
        }

        //admin does not need to specify oldPassword except when he changes his own data
        if (!isAdmin) {
            if (authorizedUser.getId() != user.getId()) {
                return new ResponseEntity("The user has not the rights to perform this action", HttpStatus.FORBIDDEN);
            } else if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                return new ResponseEntity("The supplied current password is not correct", HttpStatus.BAD_REQUEST);
            }
        } else if (authorizedUser.getId() == user.getId() && (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword()))) {
            return new ResponseEntity("The supplied current password is not correct", HttpStatus.BAD_REQUEST);
        }

        if (username != null) {
            user.setUsername(username);
        }

        if (role != null && !user.getRole().equals(role)) {
            //user can not change it's own role only admins can changes roles
            if (isAdmin) {
                user.setRole(role);
            } else {
                return new ResponseEntity("The user has not the rights to perform this action", HttpStatus.FORBIDDEN);
            }
        }

        if (password != null && !passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(password);
        }

        try {
            userService.saveUser(user);
        } catch (UserService.UsernameNotAvailableException e) {
            return new ResponseEntity("The username was already taken", HttpStatus.CONFLICT);
        } catch (UserService.EmptyPasswordException e2) {
            return new ResponseEntity("Sent password is empty", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Method that deletes a user with the specified id. Only admins are allowed to delete users.
     *
     * @param id number that represent the id of the to be deleted user
     * @return ResponseEntity: 200 if user could be deleted, 404 if user couldn't be found, 409 if user is ingame
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable Long id) {
        User userToDelete = this.userService.getUserById(id);
        if (userToDelete != null) {

            if (lobbyService.isUserInGame(userToDelete)) {
                return new ResponseEntity("User cannot be deleted because he is currently in a game.", HttpStatus.CONFLICT);
            }

            this.userService.deleteUser(userToDelete);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Returns a list of all users. Only admins are allowed to call this method.
     *
     * @return list of users
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<User> getUsers() {
        return this.userService.getAllUsers();
    }

    /**
     * Creates a user with the specified username, password and role. Only admins are allowed to create users.
     *
     * @param createUserDTO DTO that contains username, password and role
     * @return ResponseEntity: 201 if user could be created, 400 if missing parameters or empty password, 409 if there
     * already exists a user with the same password
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserDTO createUserDTO) {
        String username = createUserDTO.getUsername();
        String password = createUserDTO.getPassword();
        UserRole role = createUserDTO.getRole();
        if (username != null && password != null && role != null) {

            try {
                User createdUser = userService.saveUser(new User(username, password, role));
                return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
            } catch (UserService.UsernameNotAvailableException e) {
                return new ResponseEntity<>("The username was already taken", HttpStatus.CONFLICT);
            } catch (UserService.EmptyPasswordException e2) {
                return new ResponseEntity<>("Sent password is empty", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("User needs username, password and role", HttpStatus.BAD_REQUEST);
    }

    /**
     * Method searches users by username  based on the given search parameter.
     *
     * @param username string that is used to search usernames
     * @return a list of usernames that match search pattern
     */
    @GetMapping("search/{username}")
    public List<String> searchUsers(@PathVariable String username) {
        return this.userService.searchUsers(username);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}/ingame")
    public ResponseEntity<?> isUserInGame(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        if(user != null) {
            return new ResponseEntity<>(lobbyService.isUserInGame(user),HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

    }

}
