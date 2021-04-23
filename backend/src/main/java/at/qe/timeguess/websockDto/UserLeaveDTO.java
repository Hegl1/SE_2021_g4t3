package at.qe.timeguess.websockDto;

public class UserLeaveDTO extends AbstractDTO {

	private String userName;

	public UserLeaveDTO() {
		super();
	}

	public UserLeaveDTO(final String username) {
		this.userName = username;
	}

	public String getUserName() {
		return userName;
	}

}
