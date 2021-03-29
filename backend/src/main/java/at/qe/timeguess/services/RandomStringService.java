package at.qe.timeguess.services;

import java.security.SecureRandom;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Service to generate random Strings (strongly inspired by
 * https://mkyong.com/java/java-how-to-generate-a-random-string/)
 */

@Service
@Scope("application")
public class RandomStringService {

	private static final String UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWER_CHARS = UPPER_CHARS.toLowerCase();
	private static final String NUMBERS = "1234567890";
	private static final String SPECIAL_CHARS = "#*+-";
	private static final String ALL_CHARS = UPPER_CHARS + LOWER_CHARS + NUMBERS + SPECIAL_CHARS;

	private SecureRandom randNumGenerator;

	public RandomStringService() {
		randNumGenerator = new SecureRandom();
	}

	/**
	 * Generates a random string from alphanumeric chars + '#*+-'.
	 * 
	 * @param length length of the string to be created.
	 * @return random string.
	 */
	public String generateRandomString(final int length) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			int index = randNumGenerator.nextInt(ALL_CHARS.length());
			builder.append(ALL_CHARS.charAt(index));
		}

		return builder.toString();
	}
}
