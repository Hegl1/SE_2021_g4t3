package at.qe.timeguess.websockDto;

import java.util.List;

import at.qe.timeguess.dto.TeamDTO;

public class FinishedGameDTO implements AbstractDTO {

	private List<TeamDTO> ranking;
	private int rounds;
	private int correctExpressions;
	private int wrongExpressions;
	private long duration;

	public FinishedGameDTO(final List<TeamDTO> ranking, final int rounds, final int correctExpressions,
			final int wrongExpressions, final long duration) {
		super();
		this.ranking = ranking;
		this.rounds = rounds;
		this.correctExpressions = correctExpressions;
		this.wrongExpressions = wrongExpressions;
		this.duration = duration;
	}

	public List<TeamDTO> getRanking() {
		return ranking;
	}

	public int getRounds() {
		return rounds;
	}

	public int getCorrectExpressions() {
		return correctExpressions;
	}

	public int getWrongExpressions() {
		return wrongExpressions;
	}

	public long getDuration() {
		return duration;
	}

}
