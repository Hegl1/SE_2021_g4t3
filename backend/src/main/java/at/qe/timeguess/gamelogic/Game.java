package at.qe.timeguess.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.ExpressionService;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.StatisticsService;
import at.qe.timeguess.services.WebSocketService;
import at.qe.timeguess.websockDto.BatteryUpdateDTO;
import at.qe.timeguess.websockDto.DiceConnectionUpdateDTO;
import at.qe.timeguess.websockDto.FinishedGameDTO;
import at.qe.timeguess.websockDto.RunningDataDTO;
import at.qe.timeguess.websockDto.ScoreUpdateDTO;
import at.qe.timeguess.websockDto.StateUpdateDTO;
import at.qe.timeguess.websockDto.WaitingDataDTO;

/**
 * Class that represents am open game. Contains all the game logic.
 */
public class Game {

	private static WebSocketService webSocketService;
	private static ExpressionService expressionService;
	private static LobbyService lobbyService;
	private static StatisticsService statsService;

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
	private List<User> usersWithDevices;

	// setup phase
	private Map<User, Boolean> readyPlayers;
	private List<User> unassignedUsers;

	// ingame phase
	private int currentTeam;
	private int roundCounter;
	private Integer currentFacet;
	private List<Expression> usedExpressions;
	private Expression currentExpression;
	private Long roundStartTime;
	private Long roundEndTime;
	private long gameStartTime;
	private boolean expressionConfirmed;

	/**
	 * Minimal constructor for testing purposes
	 * 
	 * @param code the gamecode
	 */
	public Game(final int code) {
		this.teams = new ArrayList<Team>();
		this.readyPlayers = new HashMap<>();
		this.usersWithDevices = new LinkedList<User>();
		this.usedExpressions = new LinkedList<Expression>();
		this.unassignedUsers = new LinkedList<User>();
		this.active = false;
		this.gameCode = code;
		this.dice = new Dice();
		dice.setRaspberryConnected(true);
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
			current.setName("Team " + (current.getIndex() + 1));
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
			webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
			addToReadyMapIfNotAlreadyExists(player, false);
		} else if (!isInGame(player) && active) {
			throw new GameAlreadyRunningException();
		} else {
			if (!active) {
				readyPlayers.put(player, false);
				usersWithDevices.add(player);
				webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
			}
		}
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
			webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
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
			if (readyPlayers.get(player) != null && readyPlayers.get(player)) {
				// if player is ready, no switch
				return;
			}

			if (team == null) {
				leaveTeam(player);
				webSocketService.sendTeamUpdateToFrontend(gameCode, buildTeamDTOs(teams));
				webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
			} else {
				leaveTeam(player);
				if (unassignedUsers.contains(player)) {
					unassignedUsers.remove(player);
				}
				// add to ready map if offline player
				addToReadyMapIfNotAlreadyExists(player, true);
				team.joinTeam(player);
				webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
				webSocketService.sendTeamUpdateToFrontend(gameCode, buildTeamDTOs(teams));
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
			webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
		} else {
			updateReadyStatus(player, true);
			usersWithDevices.remove(player);
		}
		if (player.equals(host) || (!allTeamsEnoughPlayersWithDevice() && active)) {
			lobbyService.abortRunningGame(gameCode);
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
		if (user.equals(host) && isReady.equals(false)) {
			// hosts sets ready to false
			for (User current : usersWithDevices) {
				readyPlayers.put(current, false);
				webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
			}

		} else if (user.equals(host) && !checkGameStartable()) {
			// host trys to set ready to true, but not startable - do nothing
			webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
		} else if (unassignedUsers.contains(user)) {
			// do nothing intentionally
		} else {
			// set ready of player
			readyPlayers.put(user, isReady);
			webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
			checkAllPlayersReadyAndStartGame();
		}
	}

	/**
	 * Checks whether all players are ready. If this is the case, game gets started.
	 */
	private void checkAllPlayersReadyAndStartGame() {
		if (readyPlayers.get(host)) {
			for (User user : readyPlayers.keySet()) {
				if (!readyPlayers.get(user)) {
					return;
				}
			}
			startGame();
		}
	}

	/**
	 * Method that starts the active game phase and initializes all parameters.
	 */
	private void startGame() {
		active = true;
		expressionConfirmed = false;
		currentFacet = null;
		gameStartTime = System.currentTimeMillis() / 1000L;
		currentTeam = new Random().nextInt(numberOfTeams);
		roundCounter = 1;
		currentExpression = expressionService.getRandomExpressionByCategory(category);
		usedExpressions.add(currentExpression);
		roundStartTime = -1L;
		roundEndTime = -1L;
		webSocketService.sendCompleteGameUpdateToFrontend(gameCode, buildStateUpdate());
	}

	/**
	 * Method that is called whenever a dice gets updated and a game is mapped. Only
	 * accepts updates in appropriate situations, otherwise does nothing.
	 */
	public void diceUpdate(final int facet) {
		if (roundStartTime == -1 && dice.isRaspberryConnected()) {
			// between round phase - start timer
			roundStartTime = System.currentTimeMillis() / 1000L;
			currentFacet = facet;
			sendRunningDataToTeams();

		} else {
			if (roundStartTime != -1L && roundEndTime == -1L && dice.isRaspberryConnected()) {
				roundEndTime = System.currentTimeMillis() / 1000L;
				sendRunningDataToTeams();
			}
		}
	}

	/**
	 * Method that handles when a team confirms whether the current team guessed
	 * correct, wrong or invald. only changes gameflow in appropriate situations -
	 * in other situations it does nothing.
	 * 
	 * @param descision can either be CORRECT, IVALID or WRONG
	 */
	public synchronized void confirmExpression(final String descision) {
		if (roundStartTime != -1 && !expressionConfirmed && dice.isRaspberryConnected()) {
			expressionConfirmed = true;
			if (descision.equals("CORRECT")) {
				teams.get(currentTeam).incrementScore(dice.getPoints(currentFacet));
				teams.get(currentTeam).incrementCorrectExpressions();
				webSocketService.sendScoreChangeToFrontend(gameCode,
						new ScoreUpdateDTO(currentTeam, teams.get(currentTeam).getScore()));
				if (teams.get(currentTeam).getScore() > maxPoints) {
					finishGame();
				}
			} else if (descision.equals("INVALID")) {
				teams.get(currentTeam).decrementScore(1);
				teams.get(currentTeam).incrementWrongExpressions();
				webSocketService.sendScoreChangeToFrontend(gameCode,
						new ScoreUpdateDTO(currentTeam, teams.get(currentTeam).getScore()));
			} else {
				teams.get(currentTeam).incrementWrongExpressions();
			}
			teams.get(currentTeam).incrementCurrentPlayer();
			incrementCurrentTeam();
			if (!pickNewExpression()) {
				finishGame();
			}
			currentFacet = null;
			roundStartTime = -1L;
			roundEndTime = -1L;
			expressionConfirmed = false;
			roundCounter++;
			sendRunningDataToTeams();
		}
	}

	/**
	 * Can get called by an admin to force close the game.
	 */
	public void forceClose() {
		webSocketService.sendGameNotContinueableToFrontend(gameCode);
	}

	/**
	 * Method that finishes the game properly. Users get notfied of an early end if
	 * no expressions are left in the category.
	 */
	public void finishGame() {

		long endTime = System.currentTimeMillis() / 1000L;

		active = false;
		Collections.sort(teams, new Comparator<Team>() {
			@Override
			public int compare(final Team o1, final Team o2) {
				if (o1.getScore() < o2.getScore()) {
					return -1;
				} else if (o1.getScore() == o2.getScore()) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		FinishedGameDTO finishedGame = new FinishedGameDTO(buildTeamDTOs(teams), category.getName(),
				getTotalNumberOfCorrectExpressions(), getTotalNumberOfWrongExpressions(), endTime - gameStartTime);

		if (usedExpressions.size() == expressionService.getAllExpressionsByCategory(category).size()) {
			webSocketService.sendFinishGameToFrontend(gameCode, finishedGame, true);
		} else {
			webSocketService.sendFinishGameToFrontend(gameCode, finishedGame, false);
		}

		persistFinishedGame();
		lobbyService.closeFinishedGame(gameCode);

	}

	/**
	 * Persist all the neccessary information of the finished game
	 */
	private void persistFinishedGame() {
		statsService.persistCompletedGame(new Date(gameStartTime * 1000L), new Date(), category, teams);
	}

	/**
	 * Method that updates the dices battery status
	 */
	public void updateDiceBattery(final int batteryStatus) {
		this.dice.setBatteryPower(batteryStatus);
		webSocketService.sendBatteryUpdateToFrontend(gameCode, new BatteryUpdateDTO(batteryStatus));
	}

	/**
	 * Method that updates the dices connection status. Blocks active gameflow if
	 * dice is disconected and starts a fresh round with a fresh expression upon
	 * reconnection.
	 */
	public void updateDiceConnection(final boolean isConnected) {
		if (isConnected) {
			if (!pickNewExpression()) {
				finishGame();
			}
			webSocketService.sendConnectionUpdateToFrontend(gameCode, new DiceConnectionUpdateDTO(isConnected));
			currentFacet = null;
			roundStartTime = -1L;
			roundEndTime = -1L;
			expressionConfirmed = false;
			sendRunningDataToTeams();
			dice.setRaspberryConnected(isConnected);

		} else {
			dice.setRaspberryConnected(isConnected);
			webSocketService.sendConnectionUpdateToFrontend(gameCode, new DiceConnectionUpdateDTO(isConnected));
			roundStartTime = -1L;
			roundEndTime = -1l;
			sendRunningDataToTeams();
		}

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
	 * Method that sends running game information to the teams. The currently
	 * guessing team does not get information about the current expression;
	 */
	private void sendRunningDataToTeams() {
		for (Team t : teams) {
			if (currentTeam == t.getIndex()) {
				webSocketService.sendRunningDataToTeam(gameCode, t.getIndex(), buildRunningDataDTO(true));
			} else {
				webSocketService.sendRunningDataToTeam(gameCode, t.getIndex(), buildRunningDataDTO(false));
			}
		}
	}

	/**
	 * Method that picks a new random expression and adds it to the usedExpressions
	 * list.
	 * 
	 * @return true if expression could be found, false if no expressions are left.
	 */
	private boolean pickNewExpression() {
		if (usedExpressions.size() == expressionService.getAllExpressionsByCategory(category).size()) {
			return false;
		} else {
			while (usedExpressions.contains(currentExpression)) {
				currentExpression = expressionService.getRandomExpressionByCategory(category);
			}
			usedExpressions.add(currentExpression);
			return true;
		}
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
	 * Method that checks whether a user is in the currently guessing team.
	 * 
	 * @param user user to check for
	 * @return true if user is in the currently guessing team, else false.
	 */
	public boolean isUserInCurrentTeam(final User user) {
		return teams.get(currentTeam).getPlayers().contains(user);
	}

	/**
	 * Method that returns the player that is currently guessing.
	 * 
	 * @return
	 */
	private User getCurrentPlayer() {
		return teams.get(currentTeam).getCurrentPlayer();
	}

	/**
	 * Method that correctly increments the current team.
	 */
	private void incrementCurrentTeam() {
		currentTeam = (currentTeam + 1) % teams.size();
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

	/**
	 * Method that calculates the total number of correct expressions.
	 * 
	 * @return total number of correct expressions.
	 */
	private int getTotalNumberOfCorrectExpressions() {
		int result = 0;
		for (Team t : teams) {
			result += t.getNumberOfCorrectExpressions();
		}
		return result;
	}

	/**
	 * Method that calulcates the total number of wrong expressions.
	 * 
	 * @return total number of wrong expressions.
	 */
	private int getTotalNumberOfWrongExpressions() {
		int result = 0;
		for (Team t : teams) {
			result += t.getNumberOfCorrectExpressions();
		}
		return result;
	}

	/**
	 * Builds a List of all ready players
	 * 
	 * @return List of ready players.
	 */
	public List<User> buildReadyPlayerList() {
		List<User> result = new LinkedList<>();
		for (User current : readyPlayers.keySet()) {
			if (readyPlayers.get(current)) {
				result.add(current);
			}
		}
		return result;
	}

	/**
	 * Builds a WaitingDataDTO from current game information.
	 * 
	 * @return a correct WaitingDataDTO.
	 */
	public WaitingDataDTO buildWaitingDataDTO() {
		return new WaitingDataDTO(unassignedUsers, buildReadyPlayerList(), checkGameStartable());
	}

	/**
	 * Builds a RunningDataDTO from current game information. If isCurrendTeam is
	 * true, category is omitted.
	 * 
	 * @param isCurrendTeam true when building for currently guessing team, else
	 *                      false.
	 * @return a correct RunningDataDTO
	 */
	public RunningDataDTO buildRunningDataDTO(final boolean isCurrendTeam) {
		if (isCurrendTeam) {
			return new RunningDataDTO(roundCounter, roundEndTime, roundStartTime, currentTeam, getCurrentPlayer(), null,
					dice.getPoints(currentFacet), dice.getDurationInSeconds(currentFacet),
					dice.getActivity(currentFacet));
		} else {
			return new RunningDataDTO(roundCounter, roundEndTime, roundStartTime, currentTeam, getCurrentPlayer(),
					currentExpression.getName(), dice.getPoints(currentFacet), dice.getDurationInSeconds(currentFacet),
					dice.getActivity(currentFacet));
		}
	}

	/**
	 * Method that builds a StateUpdateDTO from currenct game information. If game
	 * is active, waiting information gets omitted and vice versa.
	 * 
	 * @return a correct StateUpdateDTO
	 */
	public StateUpdateDTO buildStateUpdate() {

		if (isActive()) {
			RunningDataDTO runningData = buildRunningDataDTO(false);
			return new StateUpdateDTO("RUNNING", null, runningData, gameCode, buildTeamDTOs(teams), host, category,
					maxPoints);
		} else {
			WaitingDataDTO waitingData = buildWaitingDataDTO();
			return new StateUpdateDTO("WAITING", waitingData, null, gameCode, buildTeamDTOs(teams), host, category,
					maxPoints);
		}

	}

	/**
	 * Method that builds a List of TeamDTOs from a List of teams.
	 * 
	 * @param teams teams to build DTOs from
	 * @return a List of TeamDTOs
	 */
	private List<TeamDTO> buildTeamDTOs(final List<Team> teams) {
		List<TeamDTO> result = new LinkedList<>();
		for (Team t : teams) {
			result.add(new TeamDTO(t.getName(), t.getScore(), buildUserDTOs(t.getPlayers()), t.getIndex()));
		}
		return result;
	}

	/**
	 * Method that builds a List of UserDTOs from a List of Users
	 * 
	 * @param users users to build DTOs from
	 * @return a List of UserDTOs
	 */
	private List<UserDTO> buildUserDTOs(final List<User> users) {
		List<UserDTO> result = new LinkedList<>();
		for (User u : users) {
			result.add(new UserDTO(u.getId(), u.getUsername(), u.getRole().toString()));
		}
		return result;
	}

	// Quick fixes for missing dependency injection into POJOS.

	@SuppressWarnings("static-access")
	public void setWebSocketController(final WebSocketService websock) {
		this.webSocketService = websock;
	}

	@SuppressWarnings("static-access")
	public void setExpressionService(final ExpressionService expServ) {
		this.expressionService = expServ;
	}

	@SuppressWarnings("static-access")
	public void setLobbyService(final LobbyService lobServ) {
		this.lobbyService = lobServ;
	}

	@SuppressWarnings("static-access")
	public void setStatisticService(final StatisticsService statsServ) {
		this.statsService = statsServ;
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

	public boolean isActive() {
		return active;
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

	public class GameCreationException extends Exception {

		private static final long serialVersionUID = 1L;

		public GameCreationException(final String message) {
			super(message);
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

}
