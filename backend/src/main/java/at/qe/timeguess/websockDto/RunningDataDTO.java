package at.qe.timeguess.websockDto;

import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.User;

public class RunningDataDTO implements AbstractDTO {

	private int round;

	private Long round_pause_time;

	private Long round_start_time;

	private int current_team;

	private UserDTO current_player;

	private String expression;

	private Integer points;

	private Integer total_time;

	private String action;

	public RunningDataDTO() {
	}

	public RunningDataDTO(final int round, final Long round_pause_time, final Long round_start_time,
			final int current_team, final User current_player, final String expression, final Integer points,
			final Integer total_time, final String action) {
		this.round = round;
		this.round_pause_time = round_pause_time;
		this.round_start_time = round_start_time;
		this.current_team = current_team;
		this.current_player = new UserDTO(current_player.getId(), current_player.getUsername(),
				current_player.getRole().toString());
		this.expression = expression;
		this.points = points;
		this.total_time = total_time;
		this.action = action;
	}

	public int getRound() {
		return round;
	}

	public Long getRound_pause_time() {
		return round_pause_time;
	}

	public Long getRound_start_time() {
		return round_start_time;
	}

	public int getCurrent_team() {
		return current_team;
	}

	public UserDTO getCurrent_player() {
		return current_player;
	}

	public String getExpression() {
		return expression;
	}

	public int getPoints() {
		return points;
	}

	public int getTotal_time() {
		return total_time;
	}

	public String getAction() {
		return action;
	}
}
