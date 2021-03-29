package at.qe.timeguess.dto;

/**
 * Class used for receiving dice updates from raspberries.
 *
 */
public class RaspberryDiceUpdate {

	private int side;

	public RaspberryDiceUpdate(final int side) {
		this.side = side;
	}

	public int getSide() {
		return side;
	}

}
