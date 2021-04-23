package at.qe.timeguess.websockDto;

public class ErrorDTO extends AbstractDTO {

	private String message;

	public ErrorDTO() {

	}

	public ErrorDTO(final String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
