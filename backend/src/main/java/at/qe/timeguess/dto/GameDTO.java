package at.qe.timeguess.dto;

import java.util.List;

import at.qe.timeguess.model.Category;

/**
 * Class used to send information about running games via REST.
 *
 */
public class GameDTO {

	private int code;
	private List<TeamDTO> teams;
	private UserDTO host;
	private Category category;

	public GameDTO() {

	}

	public GameDTO(final int code, final List<TeamDTO> teams, final UserDTO host, final Category category) {
		super();
		this.code = code;
		this.teams = teams;
		this.host = host;
		this.category = category;
	}

	public int getCode() {
		return code;
	}

	public List<TeamDTO> getTeams() {
		return teams;
	}

	public UserDTO getHost() {
		return host;
	}

	public Category getCategory() {
		return category;
	}

}
