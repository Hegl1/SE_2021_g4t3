package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import at.qe.timeguess.controllers.RaspberryController.RaspberryAlreadyInUseException;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.LobbyService;
import gamelogic.Dice;
import gamelogic.Game;
import gamelogic.Game.GameCreationException;

@SpringBootTest
public class LobbyServiceTest {

	@Autowired
	private LobbyService lobbyService;

	@Test
	public void testCreateGame() throws RaspberryAlreadyInUseException, GameCreationException {
		Game gameWOMapping = lobbyService.createGame(10, 2, new Category("Geography"), "testID1234");
		assertTrue(lobbyService.getAllRunningGames().size() == 1);
		assertTrue(gameWOMapping.getDice().getPoints(0) != 0); // test whether default mapping loaded
		assertThrows(RaspberryAlreadyInUseException.class, () -> lobbyService.createGame(10, 2, null, "testID1234"));
		Dice dice = new Dice(new int[12], new String[12], new int[12]); // array init of all 0 is guaranteed by the
																		// language spec
		Game gameWMapping = lobbyService.createGame(1, 4, null, dice, "newTestID1234");
		assertEquals(lobbyService.getAllRunningGames().size(), 2);
		assertNotEquals(gameWMapping.getDice().getPoints(0), 0);
		assertThrows(GameCreationException.class, () -> lobbyService.createGame(10, 0, null, "testID123456"));

		lobbyService.closeFinishedGame(gameWOMapping.getGameCode());
		lobbyService.closeFinishedGame(gameWMapping.getGameCode());

	}

	@Test
	public void testDeleteGame() throws RaspberryAlreadyInUseException, GameCreationException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), "testID1234");
		lobbyService.deleteRunningGame(testGame.getGameCode());
		assertTrue(lobbyService.getAllRunningGames().size() == 0);
		lobbyService.closeFinishedGame(testGame.getGameCode());
	}

	@Test
	public void closeFinishedGame() throws RaspberryAlreadyInUseException, GameCreationException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), "testID1234");
		lobbyService.deleteRunningGame(testGame.getGameCode());
		assertTrue(lobbyService.getAllRunningGames().size() == 0);
		lobbyService.closeFinishedGame(testGame.getGameCode());
	}

	@Test
	public void testJoinGame() throws RaspberryAlreadyInUseException, GameCreationException {
		Game testGame = lobbyService.createGame(10, 2, new Category("Geography"), "testID1234");
		lobbyService.joinGame(testGame.getGameCode(), new User());
		assertEquals(testGame.getUnassignedUsers().size(), 2);
		lobbyService.closeFinishedGame(testGame.getGameCode());
	}

}
