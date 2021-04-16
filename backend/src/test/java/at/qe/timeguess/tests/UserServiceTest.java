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

@SpringBootTest
public class UserServiceTest {

    private User admin;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CompletedGameTeamRepository completedGameTeamRepository;

    @BeforeEach
    public void init() throws UserService.UsernameNotAvailableException {
        this.admin = userService.getUserById(0L);
        if (this.admin == null) {
            this.admin = new User();
        }
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
        User newUser = new User("sadglsadlhgiofdghdfh","ffsdfa",UserRole.GAMEMANAGER);
        User saved = this.userService.saveUser(newUser);
        this.userService.deleteUser(newUser);
        Assertions.assertNull(this.userService.getUserById(saved.getId()));
    }

}
