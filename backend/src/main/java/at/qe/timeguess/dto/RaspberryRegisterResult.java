package at.qe.timeguess.dto;

/**
 * Class used for transmitting identifiers of freshly registered raspberries.
 *
 */
public class RaspberryRegisterResult {

	private String result;

	public RaspberryRegisterResult(final String result) {
		this.result = result;
	}

	public String getResult() {
		return this.result;
	}
}
