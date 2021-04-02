package gamelogic;

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
	private User leader;
	private int currentPlayer;
	private List<User> players;

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
		if (players.isEmpty()) {
			leader = user;
		}
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

	public int getScore() {
		return score;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public User getLeader() {
		return leader;
	}

	public void setLeader(final User leader) {
		this.leader = leader;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(final int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public List<User> getPlayers() {
		return players;
	}

}
