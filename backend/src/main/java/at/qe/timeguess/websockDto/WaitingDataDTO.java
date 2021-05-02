package at.qe.timeguess.websockDto;

import java.util.LinkedList;
import java.util.List;

import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.User;

public class WaitingDataDTO implements AbstractDTO {
	private List<UserDTO> unassigned_players;
	private List<UserDTO> ready_players;
	private boolean startable;

	public WaitingDataDTO() {

	}

	public WaitingDataDTO(final List<User> unassigned_players, final List<User> ready_players,
			final boolean startable) {
		this();
		this.unassigned_players = buildUserDTO(unassigned_players);
		this.ready_players = buildUserDTO(ready_players);
		this.startable = startable;
	}

	public List<UserDTO> getUnassigned_players() {
		return unassigned_players;
	}

	public List<UserDTO> getReady_players() {
		return ready_players;
	}

	public boolean getStartable() {
		return startable;
	}

	private List<UserDTO> buildUserDTO(final List<User> users) {
		List<UserDTO> result = new LinkedList<>();
		for (User u : users) {
			result.add(new UserDTO(u.getId(), u.getUsername(), u.getRole().toString()));
		}
		return result;
	}

}
