package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;

import at.qe.timeguess.gamelogic.Dice;
import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.gamelogic.Game.GameCreationException;
import at.qe.timeguess.gamelogic.Game.TeamIndexOutOfBoundsException;
import at.qe.timeguess.gamelogic.Game.UserStateException;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.LobbyService.GameNotFoundException;
import at.qe.timeguess.services.RaspberryService.RaspberryAlreadyInUseException;
import at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException;

@SpringBootTest
@WebAppConfiguration
public class LobbyServiceTest {

	@Autowired
	private LobbyService lobbyService;

	private static String RASPIID = "TESTRASPIID";

	@Test
	public void testCreateGame()
			throws RaspberryAlreadyInUseException, GameCreationException, RaspberryNotFoundException {
		Game gameWOMapping = lobbyService.createGame(10, 2, new Category("Geography"), RASPIID);
		assertTrue(lobbyService.getAllRunningGames().size() == 1);
		assertTrue(gameWOMapping.getDice().getPoints(0) != 0); // test whether default mapping loaded
		assertThrows(RaspberryAlreadyInUseException.class, () -> lobbyService.createGame(10, 2, null, RASPIID));
		Dice dice = new Dice(new int[12], new String[12], new int[12]); // array init of all 0 is guaranteed by the
																		// language spec
		lobbyService.closeFinishedGame(gameWOMapping.getGameCode());
		Game gameWMapping = lobbyService.createGame(1, 4, null, dice, RASPIID);
		assertEquals(lobbyService.getAllRunningGames().size(), 1);
		assertNotEquals(gameWMapping.getDice().getPoints(0), 0);
		lobbyService.closeFinishedGame(gameWMapping.getGameCode());
		assertThrows(GameCreationException.class, () -> lobbyService.createGame(10, 0, null, "testID123456"));
		assertThrows(RaspberryNotFoundException.class, () -> lobbyService.createGame(10, 2, null, "Test"));

	}

	@Test
	@WithMockUser(username = "admin")
	public void testDeleteGame()
			throws RaspberryAlreadyInUseException, GameCreationException, RaspberryNotFoundException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), RASPIID);
		lobbyService.abortRunningGame(testGame.getGameCode());
		assertTrue(lobbyService.getAllRunningGames().size() == 0);
		lobbyService.closeFinishedGame(testGame.getGameCode());
	}

	@Test
	@WithMockUser(username = "admin")
	public void closeFinishedGame()
			throws RaspberryAlreadyInUseException, GameCreationException, RaspberryNotFoundException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), RASPIID);
		lobbyService.closeFinishedGame(testGame.getGameCode());
		assertTrue(lobbyService.getAllRunningGames().size() == 0);
		lobbyService.closeFinishedGame(testGame.getGameCode());
	}

	@Test
	@WithMockUser(username = "admin")
	public void testJoinGame() throws RaspberryAlreadyInUseException, GameCreationException, RaspberryNotFoundException,
			GameNotFoundException, UserStateException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), RASPIID);
		lobbyService.joinGame(testGame.getGameCode(), new User());
		assertEquals(testGame.getUnassignedUsers().size(), 2);
		assertEquals(testGame.getUsersWithDevices().size(), 2);
		lobbyService.closeFinishedGame(testGame.getGameCode());
	}

	@Test
	@WithMockUser(username = "admin")
	public void testJoinTeam() throws RaspberryAlreadyInUseException, GameCreationException, RaspberryNotFoundException,
			GameNotFoundException, TeamIndexOutOfBoundsException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), RASPIID);
		lobbyService.joinTeam(testGame.getGameCode(), new User(), 0);
		assertEquals(testGame.getTeamByIndex(0).getPlayers().size(), 1);
		lobbyService.closeFinishedGame(testGame.getGameCode());

	}

	public void testLeaveTeam() {

	}

	public void testLeaveGame() {

	}

}
