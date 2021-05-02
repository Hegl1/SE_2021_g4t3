package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.qe.timeguess.services.WebSocketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import at.qe.timeguess.repositories.RaspberryIDRepository;
import at.qe.timeguess.services.RaspberryService;
import at.qe.timeguess.services.RaspberryService.RaspberryAlreadyInUseException;
import at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException;
import at.qe.timeguess.gamelogic.Game;

@SpringBootTest
public class RaspberryServiceTest {

	@Autowired
	private RaspberryService raspberryService;

	@Autowired
	private RaspberryIDRepository raspiRepo;

	@Autowired
    private WebSocketService webSocketService;

	@Test
	public void testRegisterGame() throws RaspberryAlreadyInUseException, RaspberryNotFoundException {
		raspberryService.registerGame("TESTRASPIID", new Game(500));
		assertThrows(RaspberryService.RaspberryAlreadyInUseException.class,
				() -> raspberryService.registerGame("TESTRASPIID", new Game(500)));
		assertEquals(raspberryService.getGameMappings().get("TESTRASPIID").getGameCode(), 500);
		raspberryService.unregisterGame("TESTRASPIID");
		assertThrows(RaspberryService.RaspberryNotFoundException.class,
				() -> raspberryService.registerGame("FalseID", null));
	}

	@Test
	public void testUnregisterGame() throws RaspberryAlreadyInUseException, RaspberryNotFoundException {
		raspberryService.registerGame("TESTRASPIID", new Game(500));
		raspberryService.unregisterGame("TESTRASPIID");
		assertNull(raspberryService.getGameMappings().get("testid"));
	}

	@DirtiesContext
	@Test
	public void testRegisterRaspberry() {
		String id = raspberryService.registerRaspberry();
		raspiRepo.findFirstById(id);
		assertNotNull(raspiRepo.findFirstById(id));
	}

	@Test
	public void testUpdateDice() throws RaspberryNotFoundException {
		assertThrows(RaspberryNotFoundException.class, () -> raspberryService.updateDice("NotUpdateable", 0));
	}

	@Test
	public void testUpdateDiceBattery() throws RaspberryAlreadyInUseException, RaspberryNotFoundException {
		Game testGame = new Game(111111111);
		webSocketService.setWebsocketControllerForGame(testGame);
		raspberryService.registerGame("TESTRASPIID", testGame);
		raspberryService.updateDiceBatteryStatus("TESTRASPIID", 69);
		assertEquals(69, testGame.getDice().getBatteryPower());
		raspberryService.unregisterGame("TESTRASPIID");
		assertThrows(RaspberryNotFoundException.class, () -> raspberryService.updateDice("NOT_FOUND", 0));
	}

	@Test
	public void testUpdateDiceConnection() throws RaspberryNotFoundException, RaspberryAlreadyInUseException {
		Game testGame = new Game(111111111);
        webSocketService.setWebsocketControllerForGame(testGame);
		assertFalse(raspberryService.updateDiceConnectionStatus("TESTRASPIID", false));
		raspberryService.registerGame("TESTRASPIID", testGame);
		assertTrue(raspberryService.updateDiceConnectionStatus("TESTRASPIID", false));
		assertEquals(testGame.getDice().isRaspberryConnected(), false);
		raspberryService.unregisterGame("TESTRASPIID");
		assertThrows(RaspberryNotFoundException.class, () -> raspberryService.updateDice("NOT_FOUND", 0));

	}

}
