package at.qe.timeguess.gamelogic;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.ExpressionService;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.StatisticsService;
import at.qe.timeguess.services.WebSocketService;

import java.util.*;

/**
 * Class that represents am open game. Contains all the game logic.
 */
public class Game {

    private static WebSocketService webSocketService;
    private static ExpressionService expressionService;
    private static LobbyService lobbyService;
    private static StatisticsService statsService;
    private IngameDTOFactory dtoFactory;

    private int gameCode;
    private String raspberryId;
    private Dice dice;
    private int maxPoints;
    private Category category;
    private User host;
    private boolean active;
    private List<Team> teams;
    private Set<User> usersWithDevices;
    private SetupGamePhase setupGamePhase;
    private RunningGamePhase runningGamePhase;

    /**
     * Minimal constructor for testing purposes
     *
     * @param code the gamecode
     */
    public Game(final int code) {
        this.teams = new ArrayList<>();
        this.usersWithDevices = new HashSet<>();
        this.active = false;
        this.gameCode = code;
        this.dice = new Dice();
        this.runningGamePhase = new RunningGamePhase(this);
        dice.setRaspberryConnected(true);
        this.dtoFactory = new IngameDTOFactory();
    }

	public Game(final int code, final int maxPoints, final int numberOfTeams, final Category category, final User host,
			final String raspberryId) throws GameCreationException {
        this(code);
        this.category = category;
        usersWithDevices.add(host);
        this.host = host;
        this.setupGamePhase = new SetupGamePhase(this);
        this.maxPoints = maxPoints;
        if (numberOfTeams < 2) {
            throw new GameCreationException("Too less teams for game construction");
        }
        for (int i = 0; i < numberOfTeams; i++) {
            final Team current = new Team();
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
	 * @throws GameAlreadyRunningException thrown when the game is already running
	 */
	public void joinGame(final User player) throws GameAlreadyRunningException {
        if (!isInGame(player) && !active) {
            setupGamePhase.joinNewUserToGame(player);
        } else if (!isInGame(player) && active) {
            throw new GameAlreadyRunningException();
        } else {
            if (!usersWithDevices.contains((player))) {
                usersWithDevices.add(player);
                if (!active) {
                    setupGamePhase.promoteNoDeviceUserToDeviceUSer(player);
                }
            }
        }
	}

	/**
	 * Method to assign a player to a team. If the player is in the unassignedUsers
	 * list or in another team, it gets removed from there.
	 *
	 * @param team   Team to move the user to
	 * @param player User to move
	 * @throws HostAlreadyReadyException thrown when the host is already ready.
	 */
	public void joinTeam(final Team team, final User player) throws HostAlreadyReadyException {
        setupGamePhase.joinTeam(team, player);
	}

	/**
	 * Method to make a player leave a game if he is not assigned to a team or set
	 * him into 'offline state' if he is assigned to a team.
	 *
	 * @param player player to leave
	 *
	 */
	public void leaveGame(final User player) {
        if (player.equals(host) || (!allTeamsEnoughPlayersWithDevice() && active)) {
            lobbyService.abortRunningGame(gameCode);
        }
        if (!active) {
            setupGamePhase.leaveNotStartedGame(player);
        }
        getWebSocketService().sendTeamUpdateToFrontend(getGameCode(), getDtoFactory().buildTeamDTOs(getTeams()));
        getWebSocketService().sendWaitingDataToFrontend(getGameCode(), getDtoFactory().buildWaitingDataDTO(this));
        getUsersWithDevices().remove(player);
    }

    /**
     * Method to update the ready status of a user. Also sends messages to frontend
     * via websocket.
     *
     * @param user    user to update the ready status of.
     * @param isReady new ready status.
     */
    public void updateReadyStatus(final User user, final Boolean isReady) {
        setupGamePhase.updateReadyStatus(user, isReady);
    }

    /**
     * Method that starts the active game phase and initializes all parameters.
     */
    protected void startGame() {
        active = true;
        runningGamePhase.startGame();
    }

	/**
	 * Method that is called whenever a dice gets updated and a game is mapped. Only
	 * accepts updates in appropriate situations, otherwise does nothing.
	 */
	public void diceUpdate(final int facet) {
        runningGamePhase.diceUpdate(facet);
    }

    /**
     * Method that handles when a team confirms whether the current team guessed
     * correct, wrong or invalid. only changes game flow in appropriate situations -
     * in other situations it does nothing.
     *
     * @param decision can either be CORRECT, INVALID or WRONG
     */
    public synchronized void confirmExpression(final String decision) {
        runningGamePhase.confirmExpression(decision);
    }

    /**
     * Can get called by an admin to force close the game.
     */
    public void forceClose() {
        webSocketService.sendGameNotContinuableToFrontend(gameCode);
    }

    /**
     * Method to persist a finished game
     */
    protected void persistFinishedGame() {
        statsService.persistCompletedGame(new Date(getGameStartTime() * 1000L), new Date(), getCategory(), getTeams());
    }

    /**
     * Method that updates the dices battery status
     */
    public void updateDiceBattery(final int batteryStatus) {
        runningGamePhase.updateDiceBattery(batteryStatus);
    }

    /**
     * Method that updates the dices connection status. Blocks active game flow if
     * dice is disconnected and starts a fresh round with a fresh expression upon
     * reconnection.
     */
    public void updateDiceConnection(final boolean isConnected) {
        runningGamePhase.updateDiceConnection(isConnected);
    }

    /**
     * Method that checks whether all teams have enough devices in their team.
     *
     * @return true if all teams have enough devices.
     */
    protected boolean allTeamsEnoughPlayersWithDevice() {
        for (final Team current : teams) {
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
        for (final User current : t.getPlayers()) {
            if (usersWithDevices.contains(current)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Method that checks whether a user is in the game.
	 *
	 * @param user user to check for
	 * @return true if the user is in the game, else false
	 */
	public boolean isInGame(final User user) {
        for (final Team t : teams) {
            if (t.getPlayers().contains(user)) {
                return true;
            }
        }
        for (final User u : setupGamePhase.getUnassignedUsers()) {
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
        return runningGamePhase.isUserInCurrentTeam(user);
    }

    /**
     * Method that returns the player that is currently guessing.
     *
     * @return the player that is currently guessing
     */
    protected User getCurrentPlayer() {
        return runningGamePhase.getCurrentPlayer();
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
        if (index >= getNumberOfTeams()) {
            throw new TeamIndexOutOfBoundsException();
        } else if (index == -1) {
            return null;
        } else {
            return this.teams.get(index);
        }
    }

    /**
     * Method that checks whether the game is startable
     *
     * @return True if the game is startable, false if not
     */
    protected boolean checkGameStartable() {
        return setupGamePhase.checkGameStartable();
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

    protected WebSocketService getWebSocketService() { return this.webSocketService; }

    protected ExpressionService getExpressionService() { return this.expressionService; }

    protected LobbyService getLobbyService() { return this.lobbyService; }

    public int getGameCode() { return this.gameCode; }

    public List<Team> getTeams() { return teams; }

    public User getHost() { return host; }

    public Category getCategory() { return category; }

    public String getRaspberryId() { return raspberryId; }

    public Dice getDice() { return dice; }

    public List<User> getUnassignedUsers() { return setupGamePhase.getUnassignedUsers(); }

    //use for testing purposes only!!
    public void setActive(boolean active) { this.active = active; }

    public int getMaxPoints() { return this.maxPoints; }

    public Set<User> getUsersWithDevices() { return this.usersWithDevices; }

    public IngameDTOFactory getDtoFactory() { return this.dtoFactory; }

    public Map<User, Boolean> getReadyPlayers() { return setupGamePhase.getReadyPlayers(); }

    public int getCurrentTeam() { return runningGamePhase.getCurrentTeam(); }

    public int getRoundCounter() { return runningGamePhase.getRoundCounter(); }

    public int getNumberOfTeams() { return teams.size(); }

    public Expression getCurrentExpression() { return runningGamePhase.getCurrentExpression(); }

    public Integer getCurrentFacet() { return runningGamePhase.getCurrentFacet(); }

    public Long getRoundStartTime() { return runningGamePhase.getRoundStartTime(); }

    public Long getRoundEndTime() { return runningGamePhase.getRoundEndTime(); }

    public long getGameStartTime() { return runningGamePhase.getGameStartTime(); }

    public boolean isActive() { return active; }

    public RunningGamePhase getRunningGamePhase() { return runningGamePhase; }


    public static class UserStateException extends Exception {
        private static final long serialVersionUID = 1L;
        public UserStateException(final String message) {
            super(message);
        }
    }

    public static class HostAlreadyReadyException extends Exception {
        private static final long serialVersionUID = 1L;
        public HostAlreadyReadyException(final String message) {
            super(message);
        }
    }

    public static class TeamIndexOutOfBoundsException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static class GameAlreadyRunningException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static class GameCreationException extends Exception {
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
        final Game other = (Game) obj;
        if (gameCode != other.gameCode) {
            return false;
        }
        return true;
    }
}
