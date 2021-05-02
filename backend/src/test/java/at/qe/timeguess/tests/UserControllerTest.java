package at.qe.timeguess.tests;

import at.qe.timeguess.controllers.UserController;
import at.qe.timeguess.dto.CreateUserDTO;
import at.qe.timeguess.dto.Login;
import at.qe.timeguess.dto.LoginResult;
import at.qe.timeguess.dto.UpdateUserDTO;
import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.repositories.UserRepository;
import at.qe.timeguess.services.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    private User admin;
    private String password;

    @BeforeEach
    public void init() {
        password =  "passwd";
        this.admin = userRepository.findFirstByUsername("admin");
        if (this.admin == null) {
            this.admin = new User();
            this.admin.setPassword(password);
            this.admin.setUsername("admin");
            this.admin.setRole(UserRole.ADMIN);
            this.userRepository.save(this.admin);
        }
    }

    @Test
    public void testLoginSuccess() {
        Login login = new Login(admin.getUsername(), "passwd");
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.login(login);
        Assertions.assertEquals(HttpStatus.OK,response.getStatusCode());
        LoginResult result = response.getBody();
        Assertions.assertEquals(admin,result.getUser());
        Pattern p = Pattern.compile("(?:\\w|[-_=])+\\.(?:\\w|[-_=])+\\.?(?:\\w|[-_.+\\/=])*$");
        Matcher m = p.matcher(result.getToken());
        Assertions.assertTrue(m.find());
    }

    @Test
    public void testLoginFalseUser() {
        Login login = new Login("somemadupusernamethatdoesntesxist","nonexisting");
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.login(login);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
    }

    @Test
    public void testLoginFalsePassword() {
        Login login = new Login(admin.getUsername(),"wrongpass");
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.login(login);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
    }

    @Test
    public void testRegisterSuccess() {
        String username = "newlyregistereduser";
        Login login = new Login(username, password);
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.register(login);
        Assertions.assertEquals(HttpStatus.CREATED,response.getStatusCode());
        LoginResult result = response.getBody();
        Assertions.assertEquals(this.userRepository.findFirstByUsername(username),result.getUser());
        Pattern p = Pattern.compile("(?:\\w|[-_=])+\\.(?:\\w|[-_=])+\\.?(?:\\w|[-_.+\\/=])*$");
        Matcher m = p.matcher(result.getToken());
        Assertions.assertTrue(m.find());
    }

    @Test
    public void testRegisterExistingUsername() {
        Login login = new Login(admin.getUsername(), password);
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.register(login);
        Assertions.assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
    }

    @Test
    public void testRegisterEmptyPassword() {
        Login login = new Login("anothernewusertoregister", "");
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.register(login);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    public void testRegisterNullValues() {
        Login login = new Login(null, null);
        ResponseEntity<LoginResult> response = (ResponseEntity<LoginResult>) userController.register(login);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    public void testGetUser() {
        ResponseEntity<User> response = (ResponseEntity<User>)userController.getUser(admin.getId());
        Assertions.assertEquals(HttpStatus.OK,response.getStatusCode());
        User user = response.getBody();
        Assertions.assertEquals(admin,user);
    }

    @Test
    public void testUserNotFound() {
        ResponseEntity response = (userController.getUser(9999999999L));
        Assertions.assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserSuccess() {
        User newUser = new User("somerandomuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(savedUser);
        UpdateUserDTO updateDTO = new UpdateUserDTO("somerandomuser12",null, "passwd",null);
        ResponseEntity response = userController.putUser(savedUser.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.OK,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserNotAuthorized() {
        User newUser = new User("testuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        UpdateUserDTO updateDTO = new UpdateUserDTO("testusernew",null, "passwd",null);
        ResponseEntity response = userController.putUser(savedUser.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateOtherUserNonAdmin() {
        User newUser = new User("testuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(savedUser);
        User otherUser = new User("testuser",password,UserRole.PLAYER);
        User otherSaved = userRepository.save(otherUser);
        UpdateUserDTO updateDTO = new UpdateUserDTO("testusernew",null, "passwd",null);
        ResponseEntity response = userController.putUser(otherSaved.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.FORBIDDEN,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserRoleNonAdmin() {
        User newUser = new User("testuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(savedUser);
        UpdateUserDTO updateDTO = new UpdateUserDTO("testusernew",null, "passwd","GAMEMANAGER");
        ResponseEntity response = userController.putUser(savedUser.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.FORBIDDEN,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserAdmin() {
        User newUser = new User("testuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(admin);
        UpdateUserDTO updateDTO = new UpdateUserDTO("testusernew",null, "passwd","GAMEMANAGER");
        ResponseEntity response = userController.putUser(savedUser.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.OK,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserWrongPassword() {
        User newUser = new User("somerandomuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(savedUser);
        UpdateUserDTO updateDTO = new UpdateUserDTO("somerandomuser12",null, "wrong",null);
        ResponseEntity response = userController.putUser(savedUser.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserNotFound() {
        authenticationService.setUserAuthentication(admin);
        UpdateUserDTO updateDTO = new UpdateUserDTO("dlskgjlsjgljagkl",null, "wrong",null);
        ResponseEntity response = userController.putUser(99999999L,updateDTO);
        Assertions.assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testUpdateUserConflict() {
        User newUser = new User("somerandomuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(admin);
        UpdateUserDTO updateDTO = new UpdateUserDTO("admin",null, "wrong",null);
        ResponseEntity response = userController.putUser(savedUser.getId(),updateDTO);
        Assertions.assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testDeleteUserSuccess() {
        User newUser = new User("somerandomuser",password,UserRole.PLAYER);
        User savedUser = userRepository.save(newUser);
        authenticationService.setUserAuthentication(admin);
        ResponseEntity response = userController.deleteUser(savedUser.getId());
        Assertions.assertEquals(HttpStatus.OK,response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testDeleteUserNotFound() {
        authenticationService.setUserAuthentication(admin);
        ResponseEntity response = userController.deleteUser(99999999L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
    }

    @Test
    public void testGetAllUsers() {
        authenticationService.setUserAuthentication(admin);
        List<User> userList = userController.getUsers();
        Assertions.assertEquals(8,userList.size() );
    }

    @Test
    @DirtiesContext
    public void testCreateUserSuccess() {
        authenticationService.setUserAuthentication(admin);
        CreateUserDTO createUserDTO = new CreateUserDTO("manager",password,"GAMEMANAGER");
        ResponseEntity<User> response = (ResponseEntity<User>) userController.createUser(createUserDTO);
        Assertions.assertEquals(HttpStatus.CREATED,response.getStatusCode());
        User user = userRepository.findFirstByUsername("manager");
        Assertions.assertEquals(user,response.getBody());
    }

    @Test
    public void testCreateUserConflict() {
        List<String> users =  userController.searchUsers("aggf");
        Assertions.assertTrue(users.isEmpty());
    }

    @Test
    public void testSearchUserFound() {
        List<String> users =  userController.searchUsers("ad");
        Assertions.assertTrue(users.contains(admin.getUsername()));
    }

    @Test
    public void testSearchUserEmpty() {
        authenticationService.setUserAuthentication(admin);
        CreateUserDTO createUserDTO = new CreateUserDTO("admin",password,"GAMEMANAGER");
        ResponseEntity<User> response = (ResponseEntity<User>) userController.createUser(createUserDTO);
        Assertions.assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
    }


}
