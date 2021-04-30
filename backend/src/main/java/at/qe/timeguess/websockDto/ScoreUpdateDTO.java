package at.qe.timeguess.websockDto;

public class ScoreUpdateDTO implements AbstractDTO {

	private int index;
	private int score;

	public ScoreUpdateDTO(final int index, final int score) {
		super();
		this.index = index;
		this.score = score;
	}

	public int getIndex() {
		return index;
	}

	public int getScore() {
		return score;
	}

}
