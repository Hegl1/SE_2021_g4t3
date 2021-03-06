package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import at.qe.timeguess.controllers.GameController;
import at.qe.timeguess.dto.CreateGame;
import at.qe.timeguess.dto.GameDTO;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
public class GameControllerTest {

	@Autowired
	private GameController gameController;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    private User admin;

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

	private static String RASPIID = "TESTRASPIID";

	@Test
	public void testCreateGame() {
        authenticationService.setUserAuthentication(this.admin);
		CreateGame successDTO = new CreateGame(RASPIID, 0, null, 2, 30);
		ResponseEntity<Integer> success = gameController.createGame(successDTO);
		assertEquals(HttpStatus.CREATED, success.getStatusCode());

		CreateGame forbiddenDTO = new CreateGame(RASPIID, 0, null, 2, 30);
		ResponseEntity<Integer> forbidden = gameController.createGame(forbiddenDTO);
		assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());

		CreateGame badRequestDTO = new CreateGame(RASPIID, 0, null, 0, 30);
		ResponseEntity<Integer> badReq = gameController.createGame(badRequestDTO);
		assertEquals(HttpStatus.BAD_REQUEST, badReq.getStatusCode());

		CreateGame notFoundDTO = new CreateGame("FalseID", 0, null, 2, 30);
		ResponseEntity<Integer> notFound = gameController.createGame(notFoundDTO);
		assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());

		gameController.forceCloseRunningGame(success.getBody());

	}

	@Test
	public void testGetAllRunningGames() {
        authenticationService.setUserAuthentication(this.admin);
		List<GameDTO> runningGames = gameController.getAllRunningGames().getBody();
		assertEquals(0, runningGames.size());
		ResponseEntity<Integer> success = gameController.createGame(new CreateGame(RASPIID, 0, null, 2, 30));
		runningGames = gameController.getAllRunningGames().getBody();
		assertEquals(1, runningGames.size());
		assertEquals(HttpStatus.OK, gameController.getAllRunningGames().getStatusCode());
		gameController.forceCloseRunningGame(success.getBody());

	}

	@Test
	public void testGetGameInfo() {
        authenticationService.setUserAuthentication(this.admin);
		ResponseEntity<Integer> success = gameController.createGame(new CreateGame(RASPIID, 0, null, 2, 30));
		ResponseEntity<GameDTO> gameInfo = gameController.getGameInfo(success.getBody());
		assertEquals(HttpStatus.OK, gameInfo.getStatusCode());
		assertEquals(gameInfo.getBody().getCode(), success.getBody());
		gameController.forceCloseRunningGame(success.getBody());

		gameInfo = gameController.getGameInfo(1111111111);
		assertEquals(HttpStatus.NOT_FOUND, gameInfo.getStatusCode());

	}

	@Test
    @WithMockUser(username = "admin")
	public void testForceCloseRunningGame() {
        authenticationService.setUserAuthentication(this.admin);
	    ResponseEntity<Integer> success = gameController.createGame(new CreateGame(RASPIID, 0, null, 2, 30));
		ResponseEntity<Void> response = gameController.forceCloseRunningGame(success.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		gameController.forceCloseRunningGame(success.getBody());
		List<GameDTO> runningGames = gameController.getAllRunningGames().getBody();
		assertEquals(0, runningGames.size());

		response = gameController.forceCloseRunningGame(1111111111);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

}
