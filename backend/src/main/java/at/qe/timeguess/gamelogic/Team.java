package at.qe.timeguess.gamelogic;

import java.util.ArrayList;
import java.util.List;

import at.qe.timeguess.model.User;

/**
 * Class that represents one team in a running game.
 *
 */
public class Team {

	private int score;
	private String name;
	private int currentPlayer;
	private List<User> players;
	private int index;
	private int numberOfCorrectExpressions;
	private int numberOfWrongExpressions;

	public Team() {
		this.score = 0;
		this.currentPlayer = 0;
		this.numberOfCorrectExpressions = 0;
		this.numberOfWrongExpressions = 0;
		this.players = new ArrayList<User>();
	}

	/**
	 * Method to join the team. First player to join gets the leader.
	 *
	 * @param user user to join the team.
	 */
	public void joinTeam(final User user) {
		players.add(user);
	}

	/**
	 * Method to leave the team.
	 *
	 * @param user
	 */
	public void leaveTeam(final User user) {
		players.remove(user);
	}

	/**
	 * Method to increment the score.
	 *
	 * @param points positive amount for increment.
	 */
	public void incrementScore(final int points) {
		this.score += points;
	}

	/**
	 * Method that decremetns the score. Score cannot be decremented to negative
	 * numbers.
	 * 
	 * @param points positive amount of decrement.
	 */
	public void decrementScore(final int points) {
		this.score -= points;
		if (score < 0) {
			score = 0;
		}
	}

	/**
	 * Method that increments number of correct expressions by one.
	 */
	public void incrementCorrectExpressions() {
		this.numberOfCorrectExpressions += 1;
	}

	/**
	 * Method that increments number of wrong expressions by one.
	 */
	public void incrementWrongExpressions() {
		this.numberOfWrongExpressions += 1;
	}

	/**
	 * Method that checks whether a user is in the team.
	 * 
	 * @param player User to check for.
	 * @return true if user is in the team, else false.
	 */
	public boolean isInTeam(final User player) {
		return players.contains(player);
	}

	/**
	 * Method that switches the currently active player in the team.
	 */
	public void incrementCurrentPlayer() {
		this.currentPlayer = (currentPlayer + 1) % players.size();
	}

	public int getScore() {
		return score;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(final int index) {
		this.index = index;
	}

	public int getCurrentPlayerIndex() {
		return currentPlayer;
	}

	public User getCurrentPlayer() {
		return players.get(currentPlayer);
	}

	public List<User> getPlayers() {
		return players;
	}

	public int getNumberOfCorrectExpressions() {
		return numberOfCorrectExpressions;
	}

	public int getNumberOfWrongExpressions() {
		return numberOfWrongExpressions;
	}

}
