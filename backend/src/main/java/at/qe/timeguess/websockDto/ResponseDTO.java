package at.qe.timeguess.websockDto;

public class ResponseDTO {

	private String identifier;

	private AbstractDTO data;

	public ResponseDTO() {

	}

	public ResponseDTO(final String identifier, final AbstractDTO data) {
		this.identifier = identifier;
		this.data = data;
	}

	public String getIdentifier() {
		return identifier;
	}

	public AbstractDTO getData() {
		return data;
	}

}
