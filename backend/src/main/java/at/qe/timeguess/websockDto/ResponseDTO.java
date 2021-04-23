package at.qe.timeguess.websockDto;

public class ResponseDTO {

	private String identifyer;

	private AbstractDTO data;

	public ResponseDTO() {

	}

	public ResponseDTO(final String identifyer, final AbstractDTO data) {
		this.identifyer = identifyer;
		this.data = data;
	}

	public String getIdentifyer() {
		return identifyer;
	}

	public AbstractDTO getData() {
		return data;
	}

}
