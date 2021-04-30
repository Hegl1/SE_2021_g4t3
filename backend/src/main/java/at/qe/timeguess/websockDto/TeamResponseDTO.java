package at.qe.timeguess.websockDto;

import java.util.List;

import at.qe.timeguess.gamelogic.Team;

public class TeamResponseDTO implements AbstractDTO {

	private List<Team> teams;

	public TeamResponseDTO() {
	}

	public TeamResponseDTO(final List<Team> teams) {
		this.teams = teams;
	}

	public List<Team> getTeams() {
		return teams;
	}

}
