package at.qe.timeguess.websockDto;

import java.util.List;

import at.qe.timeguess.dto.GameDTO;
import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;

public class StateUpdateDTO extends GameDTO implements AbstractDTO {

	private String status;
	private WaitingDataDTO waiting_data;
	private RunningDataDTO running_data;

	public StateUpdateDTO(final String status, final WaitingDataDTO waiting_data, final RunningDataDTO running_data,
			final int code, final List<TeamDTO> teams, final User host, final Category category, final int max_score) {
		super(code, teams, new UserDTO(host.getId(), host.getUsername(), host.getRole().toString()), category,
				max_score);
		this.status = status;
		this.waiting_data = waiting_data;
		this.running_data = running_data;
	}

	public String getStatus() {
		return status;
	}

	public WaitingDataDTO getWaiting_data() {
		return waiting_data;
	}

	public RunningDataDTO getRunning_data() {
		return running_data;
	}

}
