package at.qe.timeguess.websockDto;

public class PlayerReadyDTO extends AbstractDTO {

	private String userName;

	private Boolean readyStatus;

	public PlayerReadyDTO() {
		super();
	}

	public PlayerReadyDTO(final String userName, final Boolean readyStatus) {
		this();
		this.userName = userName;
		this.readyStatus = readyStatus;
	}

	public String getUserName() {
		return userName;
	}

	public Boolean getReadyStatus() {
		return readyStatus;
	}

}
