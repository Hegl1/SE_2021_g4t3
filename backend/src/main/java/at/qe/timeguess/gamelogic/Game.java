package gamelogic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.model.User;

/**
 * Class that represents am open game. Contains all the game logic.
 */
public class Game {

	// TODO websocket magic.

	private int gameCode;
	private int maxPoints;
	private int numberOfTeams;
	private Category category;
	private User host;
	private List<User> unassignedUsers;
	private boolean active;
	private int currentTeam;
	private int startTeam;
	private List<Team> teams;
	private List<Expression> usedExpressions;
	private Expression currentExpression;
	private Dice dice;
	private String raspberryId;

	// TODO maybe delete, revisit later
	public Game(final int code) {
		this.teams = new ArrayList<Team>();
		this.usedExpressions = new LinkedList<Expression>();
		this.unassignedUsers = new LinkedList<User>();
		this.active = false;
		this.gameCode = code;
		this.dice = new Dice();
	}

	public Game(final int code, final int maxPoints, final int numberOfTeams, final Category category, final User host,
			final String raspberryId) throws GameCreationException {
		this(code);
		this.category = category;
		this.host = host;
		this.unassignedUsers.add(host);
		this.maxPoints = maxPoints;
		this.numberOfTeams = numberOfTeams;
		if (numberOfTeams < 2) {
			throw new GameCreationException("Too less teams for game construction");
		}
		for (int i = 0; i < numberOfTeams; i++) {
			teams.add(new Team());
		}
		this.dice = new Dice();
		this.raspberryId = raspberryId;
	}

	public Game(final int code, final int maxPoints, final int numberOfTeams, final Category category, final User host,
			final Dice dice, final String raspberryId) throws GameCreationException {
		this(code, maxPoints, numberOfTeams, category, host, raspberryId);
		this.dice = dice;
		dice.setRaspberryConnected(true);
	}

	/**
	 * Method that is called whenever a dice gets updated and a game is mapped.
	 */
	public void diceUpdate(final int side) {
		// TODO implement with game logic
	}

	/**
	 * Can get called by an admin to force close the game. Communicate to users
	 * here.
	 */
	public void forceClose() {
		// TODO implement with game logic
	}

	/**
	 * Persist all the neccessary information of the finished game
	 */
	public void persistFinishedGame() {
		// TODO implement after game logic
	}

	/**
	 * Method that randomly picks an expression that has not been picked before.
	 */
	public void pickExpression() {
		// TODO implement after expressionservice
	}

	/**
	 * Method that updates the dices battery status
	 */
	public void updateDiceBattery(final int batteryStatus) {
		this.dice.setBatteryPower(batteryStatus);
		// TODO implement proper actions with game logic
	}

	/**
	 * Method that updates the dices connection status
	 */
	public void updateDiceConnection(final boolean isConnected) {
		this.dice.setRaspberryConnected(isConnected);
		// TODO implement proper action with game logic
	}

	/**
	 * Method to assign a new user to the game.
	 *
	 * @param player user that wants to join the game.
	 */
	public void joinGame(final User player) {
		unassignedUsers.add(player);
	}

	public int getGameCode() {
		return this.gameCode;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public User getHost() {
		return host;
	}

	public Category getCategory() {
		return category;
	}

	public String getRaspberryId() {
		return raspberryId;
	}

	public Dice getDice() {
		return dice;
	}

	public List<User> getUnassignedUsers() {
		return this.unassignedUsers;
	}

	public int getMaxPoints() {
		return this.maxPoints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + gameCode;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Game other = (Game) obj;
		if (gameCode != other.gameCode) {
			return false;
		}
		return true;
	}

	public class GameCreationException extends Exception {

		private static final long serialVersionUID = 1L;

		public GameCreationException(final String message) {
			super(message);
		}

	}

}
