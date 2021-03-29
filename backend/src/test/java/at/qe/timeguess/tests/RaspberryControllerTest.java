package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import at.qe.timeguess.controllers.RaspberryController;
import at.qe.timeguess.dto.Game;
import at.qe.timeguess.dto.RaspberryRegisterResult;
import at.qe.timeguess.repositories.RaspberryIDRepository;

@SpringBootTest
public class RaspberryControllerTest {

	@Autowired
	private RaspberryController raspiController;

	@Autowired
	private RaspberryIDRepository raspiRepo;

	@Test
	public void testRegisterGame() {
		assertTrue(raspiController.registerGame("testid", new Game("Gamecode")));
		assertFalse(raspiController.registerGame("testid", new Game("Gamecode")));
		assertEquals(raspiController.getGameMappings().get("testid").getGameCode(), "Gamecode");
		raspiController.unregisterGame("testid");
	}

	@Test
	public void testUnregisterGame() {
		raspiController.registerGame("testid", new Game("Gamecode"));
		raspiController.unregisterGame("testid");
		assertNull(raspiController.getGameMappings().get("testid"));
	}

	@Test
	public void testUpdateDice() {
		// TODO Implement when game is implemented.
	}

	@DirtiesContext
	@Test
	public void testRegisterRaspberry() {
		ResponseEntity<RaspberryRegisterResult> response = raspiController.registerRaspberry();
		RaspberryRegisterResult body = response.getBody();
		raspiRepo.findFirstById(body.getResult());
		assertNotNull(raspiRepo.findFirstById(body.getResult()));
	}

}
