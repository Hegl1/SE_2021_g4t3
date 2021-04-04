package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import at.qe.timeguess.controllers.RaspberryController;
import at.qe.timeguess.repositories.RaspberryIDRepository;

@SpringBootTest
public class RaspberryControllerTest {

	@Autowired
	private RaspberryController raspiController;

	@Autowired
	private RaspberryIDRepository raspiRepo;

	@Test
	public void testUpdateDice() {
		ResponseEntity<Void> notFoundEnt = raspiController.updateDice("NotFindable", 0);
		assertEquals(HttpStatus.NOT_FOUND, notFoundEnt.getStatusCode());
		ResponseEntity<Void> badReqEnt = raspiController.updateDice("TESTRASPIID", 12);
		assertEquals(HttpStatus.BAD_REQUEST, badReqEnt.getStatusCode());
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
