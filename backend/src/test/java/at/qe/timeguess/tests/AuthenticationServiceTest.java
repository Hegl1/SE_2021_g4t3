package at.qe.timeguess.tests;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.AuthenticationService;
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

    @BeforeEach
    public void init() {
        this.admin = new User("admin", "passwd", UserRole.ADMIN);
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
        Claim claim = authenticationService.getClaimFromToken(token.substring(7), "role");
        Assertions.assertEquals("PLAYER", claim.asString());
    }

    @Test
    public void testSubjectFromToken() {
        User other = new User("gamer","passwd",
            UserRole.GAMEMANAGER);
        String token = authenticationService.generateToken(admin, 30000L);
        String token2 = authenticationService.generateToken(other, 30000L);
        Assertions.assertEquals(admin.getUsername(), authenticationService.getSubject(token.substring(7)));
        Assertions.assertEquals(other.getUsername(), authenticationService.getSubject(token2.substring(7)));
    }
}
