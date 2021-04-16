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
    public void init() throws UserService.UsernameNotAvailableException {
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
        Assertions.assertEquals(this.admin,userService.getAuthenticatedUser());
    }

    @Test
    public void testSavingDuplicateUsername() {
        User newUser = new User("admin","passwd",UserRole.PLAYER);
        Assertions.assertThrows(UserService.UsernameNotAvailableException.class, () -> this.userService.saveUser(newUser));
    }

    @Test
    public void testSavingNewUser() throws UserService.UsernameNotAvailableException {
        User newUser = new User("Veryfun342ge","sdgdsgsfg",UserRole.GAMEMANAGER);
        Assertions.assertNotNull(this.userService.saveUser(newUser));
    }

    @Test
    public void testDeletingUser() throws UserService.UsernameNotAvailableException {
        String username = "sadglsadlhgiofdghdfh";
        User newUser = new User(username,"ffsdfa",UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);

        this.userService.deleteUser(saved);
        Assertions.assertNull(this.userService.getUserById(saved.getId()));
    }

    @Test
    public void testDeletingUserInTeam() throws UserService.UsernameNotAvailableException {
        this.userService.deleteUser(this.admin);
        Assertions.assertNull(this.userService.getUserById(this.admin.getId()));
    }

    @Test
    public void testDeletingUserTeamExists() throws UserService.UsernameNotAvailableException {
        CompletedGameTeam team = new CompletedGameTeam(3,4,5);
        team.addPlayer(this.admin);
        User newUser = new User("sdfasdfasfjzktzre","ffsdfa",UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);
        team.addPlayer(saved);
        CompletedGameTeam savedTeam = this.completedGameTeamRepository.save(team);
        this.userService.deleteUser(this.admin);
        Assertions.assertNotNull(this.completedGameTeamRepository.findFirstById(savedTeam.getId()));
        Assertions.assertEquals(1,this.completedGameTeamRepository.findFirstById(savedTeam.getId()).getPlayers().size());
    }

    @Test
    public void testPasswordEncrypted() throws UserService.UsernameNotAvailableException{
        User newUser = new User("lfkahgfioldshsklgnkldf","ffsdfa",UserRole.GAMEMANAGER);
        String clearPassword = newUser.getPassword();
        User saved = this.userService.saveUser(newUser);
        Assertions.assertTrue(passwordEncoder.matches(clearPassword,saved.getPassword()));
    }

    @Test
    public void testModifyUser() throws UserService.UsernameNotAvailableException{
        String username = "sadfsadgosdgh";
        this.admin.setUsername(username);
        User updated = this.userService.saveUser(this.admin);
        Assertions.assertEquals(username,updated.getUsername());
        Assertions.assertEquals(this.admin.getPassword(),updated.getPassword());
    }

    @Test
    public void testModifyPassword() throws UserService.UsernameNotAvailableException{
        User newUser = new User("lfkahgfioldshsklgnkldfgdfsg","ffsdfa",UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);
        String newPassword = "cool";
        saved.setPassword(newPassword);
        User modified = this.userService.saveUser(newUser);
        Assertions.assertTrue(passwordEncoder.matches(newPassword,modified.getPassword()));
    }


}
