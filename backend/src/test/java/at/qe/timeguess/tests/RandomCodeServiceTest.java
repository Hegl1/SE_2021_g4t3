package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import at.qe.timeguess.services.RandomCodeService;

@SpringBootTest
public class RandomCodeServiceTest {

	@Autowired
	private RandomCodeService randCodes;

	@Test
	public void testRandomRaspberyCode() {
		String rand = randCodes.generateRandomRaspberryCode(8);
		assertEquals(rand.length(), 8);
		String secondRand = randCodes.generateRandomRaspberryCode(8);
		assertNotEquals(rand, secondRand);
	}

	@Test
	public void testRandomGameCode() {
		int rand = randCodes.generateRandomGameCode(8);
		assertTrue(rand >= 0 && rand <= 99999999);
		int secondRand = randCodes.generateRandomGameCode(8);
		assertNotEquals(rand, secondRand);
	}

}
