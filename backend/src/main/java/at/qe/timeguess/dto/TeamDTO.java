package at.qe.timeguess.dto;

import java.util.List;

/**
 * Class to send teams from running games ({@link GameDTO} via rest.
 *
 */
public class TeamDTO {
	private String name;
	private int Score;
	private List<UserDTO> players;
	private int index;

	public TeamDTO(final String name, final int score, final List<UserDTO> players, final int index) {
		super();
		this.name = name;
		Score = score;
		this.players = players;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getScore() {
		return Score;
	}

	public List<UserDTO> getPlayers() {
		return players;
	}

	public int getIndex() {
		return index;
	}

}
