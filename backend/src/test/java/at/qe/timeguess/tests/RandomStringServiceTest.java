package at.qe.timeguess.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import at.qe.timeguess.services.RandomStringService;

@SpringBootTest
public class RandomStringServiceTest {

	@Autowired
	private RandomStringService randStrings;

	@Test
	public void testRandomString() {
		String rand = randStrings.generateRandomString(8);
		assertEquals(rand.length(), 8);
		String secondRand = randStrings.generateRandomString(8);
		assertNotEquals(rand, secondRand);
	}

}
