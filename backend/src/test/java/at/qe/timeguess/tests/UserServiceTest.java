package at.qe.timeguess.tests;

import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.repositories.CompletedGameTeamRepository;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class UserServiceTest {

    private User admin;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CompletedGameTeamRepository completedGameTeamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void init() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        this.admin = userService.getUserByUsername("admin");
        if (this.admin == null) {
            this.admin = new User();
        }
        this.admin.setPassword("passwd");
        this.admin.setUsername("admin");
        this.admin.setRole(UserRole.ADMIN);
        this.userService.saveUser(this.admin);
    }

    @Test
    public void testGetAuthenticatedUser() {
        authenticationService.setUserAuthentication(this.admin);
        Assertions.assertEquals(this.admin, userService.getAuthenticatedUser());
    }

    @Test
    public void testSavingDuplicateUsername() {
        User newUser = new User("admin", "passwd", UserRole.PLAYER);
        Assertions.assertThrows(UserService.UsernameNotAvailableException.class, () -> this.userService.saveUser(newUser));
    }

    @Test
    public void testSavingNewUser() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        User newUser = new User("Veryfun342ge", "sdgdsgsfg", UserRole.GAMEMANAGER);
        Assertions.assertNotNull(this.userService.saveUser(newUser));
    }

    @Test
    public void testDeletingUser() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        String username = "sadglsadlhgiofdghdfh";
        User newUser = new User(username, "ffsdfa", UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);

        this.userService.deleteUser(saved);
        Assertions.assertNull(this.userService.getUserById(saved.getId()));
    }

    @Test
    public void testDeletingUserInTeam() {
        this.userService.deleteUser(this.admin);
        Assertions.assertNull(this.userService.getUserById(this.admin.getId()));
    }

    @Test
    public void testDeletingUserTeamExists() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        CompletedGameTeam team = new CompletedGameTeam(3, 4, 5);
        team.addPlayer(this.admin);
        User newUser = new User("sdfasdfasfjzktzre", "ffsdfa", UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);
        team.addPlayer(saved);
        CompletedGameTeam savedTeam = this.completedGameTeamRepository.save(team);
        this.userService.deleteUser(this.admin);
        Assertions.assertNotNull(this.completedGameTeamRepository.findFirstById(savedTeam.getId()));
        Assertions.assertEquals(1, this.completedGameTeamRepository.findFirstById(savedTeam.getId()).getPlayers().size());
    }

    @Test
    public void testPasswordEncrypted() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        String clearPassword = "ffsdfa";
        User newUser = new User("lfkahgfioldshsklgnkldf", "ffsdfa", UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);
        Assertions.assertTrue(passwordEncoder.matches(clearPassword, saved.getPassword()));
    }

    @Test
    public void testModifyUser() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        String username = "sadfsadgosdgh";
        this.admin.setUsername(username);
        User updated = this.userService.saveUser(this.admin);
        Assertions.assertEquals(username, updated.getUsername());
        Assertions.assertEquals(this.admin.getPassword(), updated.getPassword());
    }

    @Test
    public void testModifyPassword() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        User newUser = new User("lfkahgfioldshsklgnkldfgdfsg", "ffsdfa", UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);
        String newPassword = "cool";
        saved.setPassword(newPassword);
        User modified = this.userService.saveUser(newUser);
        Assertions.assertTrue(passwordEncoder.matches(newPassword, modified.getPassword()));
    }

    @Test
    public void testEmptyPassword() {
        User newUser = new User("ldfghdfgilfghhkldfgfgkl", "", UserRole.GAMEMANAGER);
        Assertions.assertThrows(UserService.EmptyPasswordException.class, () -> this.userService.saveUser(newUser));
        this.admin.setPassword("");
        Assertions.assertThrows(UserService.EmptyPasswordException.class, () -> this.userService.saveUser(admin));
    }

    @Test
    public void testPasswordNull() {
        User newUser = new User("ldfghdfgilfghhkldfgfgkl", null, UserRole.GAMEMANAGER);
        Assertions.assertThrows(UserService.EmptyPasswordException.class, () -> this.userService.saveUser(newUser));
        this.admin.setPassword(null);
        Assertions.assertThrows(UserService.EmptyPasswordException.class, () -> this.userService.saveUser(admin));
    }

    @Test
    public void testSearch() throws UserService.UsernameNotAvailableException, UserService.EmptyPasswordException {
        User firstUser = new User("fun", "gddsg", UserRole.GAMEMANAGER);
        User secondUser = new User("fun123", "dfgd", UserRole.ADMIN);
        User thirdUser = new User("Funny", "dsfsdaf", UserRole.PLAYER);
        userService.saveUser(firstUser);
        userService.saveUser(secondUser);
        userService.saveUser(thirdUser);
        List<String> foundUsernames = userService.searchUsers("fun");
        Assertions.assertTrue(foundUsernames.containsAll(new ArrayList(Arrays.asList(firstUser.getUsername(),
            secondUser.getUsername(), thirdUser.getUsername()))));
    }

}
