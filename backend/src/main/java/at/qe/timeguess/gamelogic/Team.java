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

	public Team() {
		this.score = 0;
		this.currentPlayer = 0;
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
	 * Method to increment or decrement the score.
	 *
	 * @param points positive amount for increment, negative for decrement.
	 */
	public void incrementScore(final int points) {
		this.score += points;
	}

	public void decrementScore(final int points) {
		this.score -= points;
		if (points < 0) {
			score = 0;
		}
	}

	public boolean isInTeam(final User player) {
		return players.contains(player);
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

	public void incrementCurrentPlayer() {
		this.currentPlayer = (currentPlayer + 1) % players.size();
	}

	public List<User> getPlayers() {
		return players;
	}

}
