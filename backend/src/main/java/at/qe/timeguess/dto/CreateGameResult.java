package at.qe.timeguess.dto;

/**
 * Class to communicated the game code as a response for a freshly created game.
 */
public class CreateGameResult {

	private int game_code;

	public CreateGameResult(final int game_code) {
		super();
		this.game_code = game_code;
	}

	public int getGame_code() {
		return game_code;
	}

	public void setGame_code(final int game_code) {
		this.game_code = game_code;
	}

}
