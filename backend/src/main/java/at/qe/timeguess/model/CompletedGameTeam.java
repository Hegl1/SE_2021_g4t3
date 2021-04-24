package at.qe.timeguess.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

@Entity
public class CompletedGameTeam {

	@Id
	@GeneratedValue
	private long id;

	private int score;
	private int numberOfGuessedExpressions;
	private int numberOfWrongExpressions;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<User> players;

	public CompletedGameTeam() {
		this.players = new LinkedList<User>();
	}

	public CompletedGameTeam(final int numberOfGuessedExpressions, final int numberOfWrongExpressions,
			final int score) {
		this();
		this.numberOfGuessedExpressions = numberOfGuessedExpressions;
		this.numberOfWrongExpressions = numberOfWrongExpressions;
		this.score = score;
	}

	public CompletedGameTeam(final int numberOfGuessedExpresions, final int numberOfWrongExpressions, final int score,
			final Collection<User> players) {
		this(numberOfGuessedExpresions, numberOfWrongExpressions, score);
		this.players.addAll(players);
	}

    public long getId() {
        return id;
    }

    public boolean addPlayer(final User player) {
		return players.add(player);
	}

	public int getTotalNumerOfExpressions() {
		return numberOfGuessedExpressions + numberOfWrongExpressions;
	}

	public int getNumberOfGuessedExpressions() {
		return numberOfGuessedExpressions;
	}

	public void setNumberOfGuessedExpressions(final int numberOfGuessedExpressions) {
		this.numberOfGuessedExpressions = numberOfGuessedExpressions;
	}

	public int getNumberOfWrongExpressions() {
		return numberOfWrongExpressions;
	}

	public void setNumberOfWrongExpressions(final int numberOfWrongExpressions) {
		this.numberOfWrongExpressions = numberOfWrongExpressions;
	}

	public List<User> getPlayers() {
		return players;
	}

	public void setPlayers(final Collection<User> players) {
		this.players = new LinkedList<User>(players);
	}

	public int getScore() {
		return score;
	}

	public void setScore(final int score) {
		this.score = score;
	}

}
