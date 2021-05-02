package at.qe.timeguess.websockDto;

public class DiceConnectionUpdateDTO implements AbstractDTO {

	private boolean connectionStatus;

	public DiceConnectionUpdateDTO(final boolean connectionStatus) {
		this.connectionStatus = connectionStatus;
	}

	public boolean isConnectionStatus() {
		return connectionStatus;
	}

}
