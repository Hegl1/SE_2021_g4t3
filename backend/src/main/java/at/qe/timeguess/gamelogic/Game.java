package at.qe.timeguess.gamelogic;

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

	private int gameCode;
	private int maxPoints;
	private int numberOfTeams;
	private Category category;
	private User host;
	private List<User> unassignedUsers;
	private boolean active;
	private int currentTeam;
	private int startTeam;
	private List<User> usersWithDevices;
	private List<Team> teams;
	private List<Expression> usedExpressions;
	private Expression currentExpression;
	private Dice dice;
	private String raspberryId;

	// TODO maybe delete, revisit later
	public Game(final int code) {
		this.teams = new ArrayList<Team>();
		this.usersWithDevices = new LinkedList<User>();
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
		usersWithDevices.add(host);
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
		dice.setRaspberryConnected(true);
	}

	public Game(final int code, final int maxPoints, final int numberOfTeams, final Category category, final User host,
			final Dice dice, final String raspberryId) throws GameCreationException {
		this(code, maxPoints, numberOfTeams, category, host, raspberryId);
		this.dice = dice;
		dice.setRaspberryConnected(true);
	}

	/**
	 * Method to assign a new user to the game.
	 *
	 * @param player user that wants to join the game.
	 */
	public void joinGame(final User player) throws UserStateException {
		if (!isInGame(player)) {
			usersWithDevices.add(player);
			unassignedUsers.add(player);
		} else {
			throw new UserStateException("User already in game, ui update required");
		}
	}

	public void joinTeam(final Team team, final User player) {
		leaveTeam(player);
		team.joinTeam(player);
	}

	public void leaveTeam(final User player) {
		for (Team current : teams) {
			if (current.isInTeam(player)) {
				current.leaveTeam(player);
				break;
			}
		}
		unassignedUsers.add(player);
	}

	public void leaveGame(final User player) throws GameNotContinuableException {
		unassignedUsers.remove(player);
		if (player.equals(host) || !allTeamsEnoughPlayersWithDevice()) {
			throw new GameNotContinuableException("The host left the game or one Team has no devices left");
		} else {
			leaveTeam(player);
		}

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

	public void finishGame() {

	}

	/**
	 * Persist all the neccessary information of the finished game
	 */
	private void persistFinishedGame() {
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

	public List<User> getUsersWithDevices() {
		return this.usersWithDevices;
	}

	public boolean isInGame(final User user) {
		System.out.println(user.getUsername());
		for (Team t : teams) {
			if (t.getPlayers().contains(user)) {
				return true;
			}
		}

		for (User u : unassignedUsers) {
			if (u.equals(user)) {
				return true;
			}
		}

		return false;
	}

	private boolean allTeamsEnoughPlayersWithDevice() {
		for (Team current : teams) {
			if (!hasEnoughPlayersWithDevices(current)) {
				return false;
			}
		}

		return true;
	}

	private boolean hasEnoughPlayersWithDevices(final Team t) {
		for (User current : t.getPlayers()) {
			if (usersWithDevices.contains(current)) {
				return true;
			}
		}
		return false;
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

	public Team getTeamByIndex(final Integer index) throws TeamIndexOutOfBoundsException {
		if (index >= numberOfTeams) {
			throw new TeamIndexOutOfBoundsException();
		} else {
			return this.teams.get(index);
		}
	}

	public class GameCreationException extends Exception {

		private static final long serialVersionUID = 1L;

		public GameCreationException(final String message) {
			super(message);
		}

	}

	public class UserStateException extends Exception {
		private static final long serialVersionUID = 1L;

		public UserStateException(final String message) {
			super(message);
		}
	}

	public class GameNotContinuableException extends Exception {
		private static final long serialVersionUID = 1L;

		public GameNotContinuableException(final String message) {
			super(message);
		}
	}

	public class TeamIndexOutOfBoundsException extends Exception {

		private static final long serialVersionUID = 1L;

	}

}
