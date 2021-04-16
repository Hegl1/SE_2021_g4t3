package at.qe.timeguess.dto;

public class TestDTO {

	private String from;

	private String text;

	public TestDTO(final String from, final String text) {
		super();
		this.from = from;
		this.text = text;
	}

	public String getFrom() {
		return from;
	}

	public String getText() {
		return text;
	}

}
