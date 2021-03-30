package at.qe.timeguess.dto;

import java.util.List;

/**
 * Class to send a collection of all currently running games via REST.
 */
public class AllGameDTO {

	List<GameDTO> games;

	public AllGameDTO(final List<GameDTO> games) {
		this.games = games;
	}

	public List<GameDTO> getGames() {
		return games;
	}

}
