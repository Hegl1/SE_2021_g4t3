package at.qe.timeguess.gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.qe.timeguess.controllers.WebsocketController;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.model.User;
import at.qe.timeguess.websockDto.WaitingDataDTO;

/**
 * Class that represents am open game. Contains all the game logic.
 */
public class Game {

	private static WebsocketController webSocketController;

	// general game information
	private int gameCode;
	private String raspberryId;
	private Dice dice;
	private int maxPoints;
	private int numberOfTeams;
	private Category category;
	private User host;
	private boolean active;
	private List<Team> teams;

	// setup phase
	private Map<User, Boolean> readyPlayers;
	private List<User> unassignedUsers;
	private List<User> usersWithDevices;

	// ingame phase
	private int currentTeam;
	private int startTeam;
	private int roundCounter;
	private List<Expression> usedExpressions;
	private Expression currentExpression;
	private long roundStartTime;
	private long roundEndTime;

	// TODO maybe delete, revisit later
	public Game(final int code) {
		this.teams = new ArrayList<Team>();
		this.readyPlayers = new HashMap<>();
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
		readyPlayers.put(host, false);
		this.unassignedUsers.add(host);
		this.maxPoints = maxPoints;
		this.numberOfTeams = numberOfTeams;
		if (numberOfTeams < 2) {
			throw new GameCreationException("Too less teams for game construction");
		}
		for (int i = 0; i < numberOfTeams; i++) {
			Team current = new Team();
			teams.add(current);
			current.setIndex(teams.indexOf(current));
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
	 * @throws GameAlreadyRunningException
	 */
	public void joinGame(final User player) throws GameAlreadyRunningException {
		if (!isInGame(player) && !active) {
			usersWithDevices.add(player);
			unassignedUsers.add(player);
			webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
			addToReadyMapIfNotAlreadyExists(player, false);
		} else if (!isInGame(player) && active) {
			throw new GameAlreadyRunningException();
		} else {
			if (!active) {
				readyPlayers.put(player, false);
			}
		}
	}

	/**
	 * Method to assign a player to a team. If the player is in the unassignedUsers
	 * list or in another team, it gets removed from there.
	 * 
	 * @param team   Team to move the user to
	 * @param player User to move
	 * @throws HostAlreadyReadyException
	 */
	public void joinTeam(final Team team, final User player) throws HostAlreadyReadyException {
		if (!readyPlayers.get(host)) {

			if (readyPlayers.get(player)) {
				// if player is ready, no switch
				return;
			}

			if (team == null) {
				leaveTeam(player);
				webSocketController.sendTeamUpdateToFrontend(gameCode, teams);
				webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
			} else {
				leaveTeam(player);
				if (unassignedUsers.contains(player)) {
					unassignedUsers.remove(player);
				}
				// add to ready map if offline player
				addToReadyMapIfNotAlreadyExists(player, true);
				team.joinTeam(player);
				webSocketController.sendTeamUpdateToFrontend(gameCode, teams);
			}
		} else {
			throw new HostAlreadyReadyException("Host is already ready");
		}

	}

	/**
	 * Method to make a player leave a team and add it to the unassigned list.
	 * 
	 * @param player player to unassign.
	 * @throws HostAlreadyReadyException
	 */
	private void leaveTeam(final User player) throws HostAlreadyReadyException {
		if (!readyPlayers.get(host)) {
			for (Team current : teams) {
				if (current.isInTeam(player)) {
					unassignedUsers.add(player);
					current.leaveTeam(player);
					break;
				}
			}
		} else {
			throw new HostAlreadyReadyException("Host is already ready");
		}

	}

	/**
	 * Method to make a player leave a game if he is not assigned to a team or set
	 * him into 'offline state' if he is assigned to a team.
	 * 
	 * @param player player to leave
	 * 
	 */
	public void leaveGame(final User player) {
		if (unassignedUsers.contains(player)) {
			unassignedUsers.remove(player);
			readyPlayers.remove(player);
			usersWithDevices.remove(player);
			webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
		} else {
			updateReadyStatus(player, true);
			usersWithDevices.remove(player);
		}
		if (player.equals(host) || (!allTeamsEnoughPlayersWithDevice() && active)) {
			webSocketController.sendGameNotContinueableToFrontend(gameCode);
		}
	}

	/**
	 * Method to update the ready status of a user. Also sneds messages to frontend
	 * via websocket.
	 * 
	 * @param user    user to update the ready status of.
	 * @param isReady new ready status.
	 */
	public void updateReadyStatus(final User user, final Boolean isReady) {
		// TODO test readying logic with frontend
		if (user.equals(host) && isReady.equals(false)) {
			// hosts sets ready to false
			for (User current : usersWithDevices) {
				readyPlayers.put(current, false);
				webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
			}

		} else if (user.equals(host) && !checkGameStartable()) {
			// host trys to set ready to true, but not startable - do nothing
			System.out.println("in updateREadyStatus" + webSocketController);
			webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
		} else if (unassignedUsers.contains(user)) {
			// do nothing intentionally
		} else {
			// set ready of player
			readyPlayers.put(user, isReady);
			webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
			checkAllPlayersReadyAndStartGame();
		}
	}

	private void startGame() {
		active = true;
		// TODO proper starting action and communication with frontend
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
	 * Method to properly finish the game
	 */
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

	public List<User> buildReadyPlayerList() {
		List<User> result = new LinkedList<>();
		for (User current : readyPlayers.keySet()) {
			if (readyPlayers.get(current)) {
				result.add(current);
			}
		}
		return result;
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

	private void checkAllPlayersReadyAndStartGame() {
		if (readyPlayers.get(host)) {
			for (User user : readyPlayers.keySet()) {
				if (!readyPlayers.get(user)) {
					return;
				}
			}
		}
		startGame();
	}

	public List<User> getUsersWithDevices() {
		return this.usersWithDevices;
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * Method that checks whether a user is in the game.
	 * 
	 * @param user user to check for
	 * @return true if the user is in the game, else false
	 */
	public boolean isInGame(final User user) {
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

	/**
	 * Method that checks whether all teams have enough devices in their team.
	 * 
	 * @return true if all teams have enough devices.
	 */
	private boolean allTeamsEnoughPlayersWithDevice() {
		for (Team current : teams) {
			if (!hasEnoughPlayersWithDevices(current)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Method that checks whether a team has enough devices.
	 * 
	 * @param t team to check
	 * @return true if at least one device is in the team.
	 */
	private boolean hasEnoughPlayersWithDevices(final Team t) {
		for (User current : t.getPlayers()) {
			if (usersWithDevices.contains(current)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method that checks whether all conditions for a game start are satisfied.
	 * 
	 * @return true iff no unassigneds, enough devices and all teams > 2 users.
	 */
	private boolean checkGameStartable() {
		boolean unassigneds = this.unassignedUsers.size() == 0;
		boolean devices = allTeamsEnoughPlayersWithDevice();
		boolean teamSizes = true;
		for (Team t : teams) {
			if (t.getPlayers().size() < 2) {
				teamSizes = false;
				break;
			}
		}
		return unassigneds && devices && teamSizes;
	}

	/**
	 * Method that adds a player to the ready map if he is not already in.
	 * 
	 * @param player      player to add
	 * @param readyStatus status to add the player with
	 */
	private void addToReadyMapIfNotAlreadyExists(final User player, final boolean readyStatus) {
		if (!readyPlayers.containsKey(player)) {
			readyPlayers.put(player, readyStatus);
			System.out.println("gamecode" + gameCode);
			System.out.println(buildWaitingDataDTO());
			System.out.println(webSocketController);
			webSocketController.updateReadyInFrontend(gameCode, buildWaitingDataDTO());
		}
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

	public void setWebSocketController(final WebsocketController websock) {
		this.webSocketController = websock;
	}

	/**
	 * Method that returns a team by its index.
	 * 
	 * @param index index of the team
	 * @return team at index
	 * @throws TeamIndexOutOfBoundsException when index is bigger then number of
	 *                                       teams
	 */
	public Team getTeamByIndex(final Integer index) throws TeamIndexOutOfBoundsException {
		if (index >= numberOfTeams) {
			throw new TeamIndexOutOfBoundsException();
		} else if (index == -1) {
			return null;
		} else {
			return this.teams.get(index);
		}
	}

	public WaitingDataDTO buildWaitingDataDTO() {
		return new WaitingDataDTO(unassignedUsers, buildReadyPlayerList(), checkGameStartable());
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

	public class HostAlreadyReadyException extends Exception {
		private static final long serialVersionUID = 1L;

		public HostAlreadyReadyException(final String message) {
			super(message);
		}
	}

	public class TeamIndexOutOfBoundsException extends Exception {

		private static final long serialVersionUID = 1L;

	}

	public class GameAlreadyRunningException extends Exception {
		private static final long serialVersionUID = 1L;
	}

}
