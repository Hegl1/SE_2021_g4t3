package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import at.qe.timeguess.controllers.RaspberryController;
import at.qe.timeguess.controllers.RaspberryController.RaspberryAlreadyInUseException;
import at.qe.timeguess.controllers.RaspberryController.RaspberryNotFoundException;
import at.qe.timeguess.repositories.RaspberryIDRepository;
import at.qe.timeguess.gamelogic.Game;

@SpringBootTest
public class RaspberryControllerTest {

	@Autowired
	private RaspberryController raspiController;

	@Autowired
	private RaspberryIDRepository raspiRepo;

	@Test
	public void testRegisterGame() throws RaspberryAlreadyInUseException, RaspberryNotFoundException {
		raspiController.registerGame("TESTRASPIID", new Game(500));
		assertThrows(RaspberryController.RaspberryAlreadyInUseException.class,
				() -> raspiController.registerGame("TESTRASPIID", new Game(500)));
		assertEquals(raspiController.getGameMappings().get("TESTRASPIID").getGameCode(), 500);
		raspiController.unregisterGame("TESTRASPIID");
		assertThrows(RaspberryController.RaspberryNotFoundException.class,
				() -> raspiController.registerGame("FalseID", null));
	}

	@Test
	public void testUnregisterGame() throws RaspberryAlreadyInUseException, RaspberryNotFoundException {
		raspiController.registerGame("TESTRASPIID", new Game(500));
		raspiController.unregisterGame("TESTRASPIID");
		assertNull(raspiController.getGameMappings().get("testid"));
	}

	@Test
	public void testUpdateDice() {
		// TODO Implement when game is implemented.
	}

	@DirtiesContext
	@Test
	public void testRegisterRaspberry() {
		ResponseEntity<String> response = raspiController.registerRaspberry();
		String body = response.getBody();
		raspiRepo.findFirstById(body);
		assertNotNull(raspiRepo.findFirstById(body));
	}

}
