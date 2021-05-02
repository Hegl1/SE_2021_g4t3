package at.qe.timeguess.websockDto;

import java.util.List;

import at.qe.timeguess.dto.TeamDTO;

public class TeamResponseDTO implements AbstractDTO {

	private List<TeamDTO> teams;

	public TeamResponseDTO() {
	}

	public TeamResponseDTO(final List<TeamDTO> teams) {
		this.teams = teams;
	}

	public List<TeamDTO> getTeams() {
		return teams;
	}

}
