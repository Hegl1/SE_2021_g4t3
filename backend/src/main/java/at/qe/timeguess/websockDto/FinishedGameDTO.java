package at.qe.timeguess.websockDto;

import java.util.List;

import at.qe.timeguess.dto.TeamDTO;

public class FinishedGameDTO implements AbstractDTO {

	List<TeamDTO> ranking;
	String category;
	int correctExpressions;
	int wrongExpressions;
	long duration;

	public FinishedGameDTO(final List<TeamDTO> ranking, final String category, final int correctExpressions,
			final int wrongExpressions, final long duration) {
		super();
		this.ranking = ranking;
		this.category = category;
		this.correctExpressions = correctExpressions;
		this.wrongExpressions = wrongExpressions;
		this.duration = duration;
	}

	public List<TeamDTO> getRanking() {
		return ranking;
	}

	public String getCategory() {
		return category;
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
