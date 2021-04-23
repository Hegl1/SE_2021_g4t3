package at.qe.timeguess.websockDto;

public class JoinTeamDTO extends AbstractDTO {

	private int teamIndex;

	private String userName;

	public JoinTeamDTO() {

	}

	public JoinTeamDTO(final int teamIndex, final String userName) {
		this();
		this.teamIndex = teamIndex;
		this.userName = userName;
	}

	public int getTeamIndex() {
		return teamIndex;
	}

	public String getUserName() {
		return userName;
	}

}
