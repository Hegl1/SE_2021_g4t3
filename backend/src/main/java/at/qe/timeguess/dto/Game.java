package at.qe.timeguess.dto;

/**
 * Dummy implementation of game class - to be changed or moved later. Must
 * contain the diceUpdate method for proper communication with the dice.
 */
public class Game {

	private String gameCode;

	public Game(final String code) {
		this.gameCode = code;
	}

	/**
	 * Method that is called whenever a dice gets updated and a game is mapped.
	 */
	public void diceUpdate(final int side) {

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameCode == null) ? 0 : gameCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Game other = (Game) obj;
		if (gameCode == null) {
			if (other.gameCode != null) {
				return false;
			}
		} else if (!gameCode.equals(other.gameCode)) {
			return false;
		}
		return true;
	}

}
