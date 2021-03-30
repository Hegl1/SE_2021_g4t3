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
public class RandomCodeService {

	private static final String UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWER_CHARS = UPPER_CHARS.toLowerCase();
	private static final String NUMBERS = "1234567890";
	private static final String SPECIAL_CHARS = "#*+-";
	private static final String ALL_CHARS = UPPER_CHARS + LOWER_CHARS + NUMBERS + SPECIAL_CHARS;

	private SecureRandom randNumGenerator;

	public RandomCodeService() {
		randNumGenerator = new SecureRandom();
	}

	/**
	 * Generates a random string from alphanumeric chars + '#*+-'. Can be used as
	 * Raspberry identifiers.
	 * 
	 * @param length length of the string to be created.
	 * @return random string.
	 */
	public String generateRandomRaspberryCode(final int length) {
		return generateRandomString(length, ALL_CHARS);
	}

	/**
	 * Generates a random integer with the given length (number of digits). Can be
	 * used to identify games.
	 * 
	 * @param codeLength number of digits the integer should have.
	 * @return random integer.
	 */
	public int generateRandomGameCode(final int codeLength) {
		String codeAsString = generateRandomString(codeLength, NUMBERS);
		return Integer.parseInt(codeAsString);
	}

	/**
	 * Generates a random String from a given set of available characters.
	 * 
	 * @param length  length of the String to be created.
	 * @param charSet String to choose random chars from.
	 * @return a random string.
	 */
	private String generateRandomString(final int length, final String charSet) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int index = randNumGenerator.nextInt(charSet.length());
			builder.append(charSet.charAt(index));
		}
		return builder.toString();
	}
}
