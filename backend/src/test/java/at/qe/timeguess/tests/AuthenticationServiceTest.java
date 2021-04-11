package at.qe.timeguess.tests;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import com.auth0.jwt.interfaces.Claim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthenticationServiceTest {

    private User admin;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void init() {
        this.admin = userService.getUserById(0L);
        this.admin.setUsername("admin");
        this.admin.setRole(UserRole.ADMIN);
        this.userService.saveUser(this.admin);
    }

    @Test
    public void testTokenExpired() {
        String token = authenticationService.generateToken(admin, 0L);
        Assertions.assertFalse(authenticationService.validateToken(token.substring(7), this.admin));
    }

    @Test
    public void testTokenValidAndNotExpired() {
        String token = authenticationService.generateToken(admin, 30000L);
        Assertions.assertTrue(authenticationService.validateToken(token.substring(7), this.admin));
    }

    @Test
    public void testTokenWrongUser() {
        User other = new User("cool", "pass",UserRole.ADMIN);
        String token = authenticationService.generateToken(admin, 30000L);
        Assertions.assertFalse(authenticationService.validateToken(token.substring(7), other));
    }

    @Test
    public void testRetrieveClaims() {
        User other = new User("player","", UserRole.PLAYER);
        String token = authenticationService.generateToken(other, 30000L);
        Claim claim = authenticationService.getClaimFromToken(token.substring(7), "user_role");
        Assertions.assertEquals("PLAYER", claim.asString());
    }

    @Test
    public void testOutdatedToken() {
        String token = authenticationService.generateToken(admin, 30000L);
        admin.setUsername("dagerhgdfherh");
        userService.saveUser(admin);
        Assertions.assertFalse(authenticationService.validateToken(token.substring(7), this.admin));
        token = authenticationService.generateToken(admin, 30000L);
        Assertions.assertTrue(authenticationService.validateToken(token.substring(7), this.admin));
    }

    @Test
    public void testTokenWithFixedExpiration() {
        String token = authenticationService.generateTokenWithFixedExpiration(this.admin);
        Assertions.assertTrue(authenticationService.validateToken(token.substring(7), this.admin));
    }


}
