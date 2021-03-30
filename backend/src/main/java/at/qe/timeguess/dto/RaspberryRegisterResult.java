package at.qe.timeguess.dto;

/**
 * Class used for transmitting identifiers of freshly registered raspberries.
 *
 */
public class RaspberryRegisterResult {

	private String identifier;

	public RaspberryRegisterResult(final String result) {
		this.identifier = result;
	}

	public String getResult() {
		return this.identifier;
	}
}
