package at.qe.timeguess.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class CompletedGame {

	@Id
	@GeneratedValue
	private long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

	@OneToOne
	private Category category;

	@OneToMany
	private List<CompletedGameTeam> attendedTeams;

	public CompletedGame() {
		this.attendedTeams = new LinkedList<CompletedGameTeam>();
	}

	public CompletedGame(final Date startTime, final Date endTime, final Category category) {
		this();
		this.startTime = startTime;
		this.endTime = endTime;
		this.category = category;
	}

	public CompletedGame(final Date startTime, final Date endTime, final Category category,
			final Collection<CompletedGameTeam> teams) {
		this(startTime, endTime, category);
		this.attendedTeams.addAll(teams);
	}

	public int getMaxScore() {
		int max = 0;
		for (CompletedGameTeam team : attendedTeams) {
			if (team.getScore() > max) {
				max = team.getScore();
			}
		}
		return max;
	}

	public int getTotalNumberOfExpressions() {
		int numberOfExpressions = 0;
		for (CompletedGameTeam team : attendedTeams) {
			numberOfExpressions += team.getNumberOfGuessedExpressions();
			numberOfExpressions += team.getNumberOfWrongExpressions();
		}
		return numberOfExpressions;
	}

	public int getTotalNumberOfGuessedExpressions() {
		int numberOfGuessedExpressions = 0;
		for (CompletedGameTeam team : attendedTeams) {
			numberOfGuessedExpressions += team.getNumberOfGuessedExpressions();
		}
		return numberOfGuessedExpressions;
	}

	public int getTotalNumberOfWrongExpressions() {
		return getTotalNumberOfExpressions() - getTotalNumberOfGuessedExpressions();
	}

	public long getDuration() {
		return endTime.getTime() - startTime.getTime();
	}

	public float getMaxScorePerTime() {
		return (float) getMaxScore() / getDuration();
	}

	public boolean addTeam(final CompletedGameTeam team) {
		return this.attendedTeams.add(team);
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(final Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(final Date endTime) {
		this.endTime = endTime;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(final Category category) {
		this.category = category;
	}

	public List<CompletedGameTeam> getAttendedTeams() {
		return attendedTeams;
	}

	public void setAttendedTeams(final List<CompletedGameTeam> attendedTeams) {
		this.attendedTeams = attendedTeams;
	}

}
