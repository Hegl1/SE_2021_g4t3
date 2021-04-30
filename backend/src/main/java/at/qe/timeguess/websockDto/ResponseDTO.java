package at.qe.timeguess.websockDto;

public class ResponseDTO {

	private String identifier;

	private AbstractDTO data;

	public ResponseDTO() {

	}

	public ResponseDTO(final String identifyer, final AbstractDTO data) {
		this.identifier = identifyer;
		this.data = data;
	}

	public String getIdentifyer() {
		return identifier;
	}

	public AbstractDTO getData() {
		return data;
	}

}
