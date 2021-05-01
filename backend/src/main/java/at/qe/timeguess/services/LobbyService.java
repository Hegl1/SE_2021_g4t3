package at.qe.timeguess.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import at.qe.timeguess.gamelogic.Dice;
import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.gamelogic.Game.GameAlreadyRunningException;
import at.qe.timeguess.gamelogic.Game.GameCreationException;
import at.qe.timeguess.gamelogic.Game.UserStateException;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.RaspberryService.RaspberryAlreadyInUseException;
import at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException;

/**
 * Class that holds and manages all games.
 */
@Service
@Scope("application")
public class LobbyService {

	@Autowired
	private UserService userService;

	@Autowired
	private RandomCodeService codeService;

	@Autowired
	private RaspberryService raspberryService;

	private ExpressionService expService;

	private WebSocketService webSocketController;

	/**
	 * Map that holds all open games.
	 */
	private Map<Integer, Game> runningGames;

	/**
	 * constant for generating game codes;
	 */
	private static final int MAX_GAME_CODE_LENGTH = 8;

	public LobbyService(@Lazy final WebSocketService controller, @Lazy final ExpressionService expService) {
		this.runningGames = new HashMap<Integer, Game>();
		this.webSocketController = controller;
		this.expService = expService;
	}

	/**
	 * Creates a game with the currently logged in user as host, a randomly
	 * generated gamecode and the default dice mapping and adds it to the
	 * runningGames index. Also registers a game to get updates from a raspberry.
	 *
	 * @param maxPoints     amount of points necessary to win.
	 * @param numberOfTeams number of teams the game should host.
	 * @param category      category of the game.
	 * @return the representation of the game.
	 * @throws RaspberryAlreadyInUseException when raspberry is already assigned to
	 *                                        a running game.
	 * @throws GameCreationException
	 * @throws RaspberryNotFoundException
	 */
	public Game createGame(final int maxPoints, final int numberOfTeams, final Category category,
			final String raspberryId) throws at.qe.timeguess.services.RaspberryService.RaspberryAlreadyInUseException,
			GameCreationException, at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException {
		Game newGame = new Game(generateGameCode(), maxPoints, numberOfTeams, category,
				userService.getAuthenticatedUser(), raspberryId);
		raspberryService.registerGame(raspberryId, newGame);
		webSocketController.setWebsocketControllerForGame(newGame);
		newGame.setExpressionService(expService);
		newGame.setLobbyService(this);
		runningGames.put(newGame.getGameCode(), newGame);
		return newGame;
	}

	/**
	 * Creates a game with the currently logged in user as host, a randomly
	 * generated gamecode and the given dice mapping and adds it to the runningGames
	 * index. Also registers a game to get updates from a raspberry.
	 *
	 * @param maxPoints     amount of points necessary to win.
	 * @param numberOfTeams number of teams the game should host.
	 * @param category      category of the game.
	 * @param dice          dice representation that contains the facet mapping.
	 * @return the representation of the game.
	 * @throws RaspberryAlreadyInUseException when raspberry is already assigned to
	 *                                        a running game.
	 * @throws GameCreationException
	 * @throws RaspberryNotFoundException
	 */
	public Game createGame(final int maxPoints, final int numberOfTeams, final Category category, final Dice dice,
			final String raspberryId)
			throws RaspberryAlreadyInUseException, GameCreationException, RaspberryNotFoundException {
		Game newGame = new Game(generateGameCode(), maxPoints, numberOfTeams, category,
				userService.getAuthenticatedUser(), raspberryId);
		raspberryService.registerGame(raspberryId, newGame);
		webSocketController.setWebsocketControllerForGame(newGame);
		newGame.setExpressionService(expService);
		newGame.setLobbyService(this);
		runningGames.put(newGame.getGameCode(), newGame);
		return newGame;
	}

	/**
	 * Generates a game code which is not already used.
	 *
	 * @return the generated game code.
	 */
	private int generateGameCode() {
		int gamecode;
		do {
			gamecode = codeService.generateRandomGameCode(MAX_GAME_CODE_LENGTH);
		} while (runningGames.containsKey(gamecode));
		return gamecode;
	}

	/**
	 * Method to assign a user to a running game.
	 *
	 * @param gameCode code of the running game. If there is no running game with
	 *                 this code, nothing happens.
	 * @param user
	 * @throws GameNotFoundException
	 * @throws UserStateException
	 * @throws GameAlreadyRunningException
	 */
	public void joinGame(final int gameCode, final User user)
			throws GameNotFoundException, GameAlreadyRunningException {
		if (runningGames.containsKey(gameCode)) {
			runningGames.get(gameCode).joinGame(user);
		} else {
			throw new GameNotFoundException();
		}
	}

	public void updateReadyStatus(final int gameCode, final User user, final Boolean isReady) {
		if (runningGames.containsKey(gameCode)) {
			runningGames.get(gameCode).updateReadyStatus(user, isReady);
		}
	}

	/**
	 * Removes a game from the index. Not to be used to forcefully close a game.
	 *
	 * @param gameCode code of running game
	 */
	public void closeFinishedGame(final int gameCode) {
		if (runningGames.containsKey(gameCode)) {
			raspberryService.unregisterGame(runningGames.get(gameCode).getRaspberryId());
			runningGames.remove(gameCode);
		}
	}

	/**
	 * Forcefully elimantes a game from the index.
	 * 
	 * @param gameCode code of running game
	 */
	public void abortRunningGame(final int gameCode) {
		if (runningGames.containsKey(gameCode)) {
			runningGames.get(gameCode).forceClose();
			raspberryService.unregisterGame(runningGames.get(gameCode).getRaspberryId());
			runningGames.remove(gameCode);
		}
	}

	/**
	 * Method to get all running games
	 *
	 * @return a collection of all running games.
	 */
	public Collection<Game> getAllRunningGames() {
		return runningGames.values();
	}

	/**
	 * Returns a running game
	 *
	 * @param gamecode code of the game to get
	 * @return game with the given code
	 */
	public Game getGame(final int gamecode) {
		return runningGames.get(gamecode);
	}

	/**
	 * Checks whether a user is assigned to a game
	 * 
	 * @param user the user to check for
	 * @return true if user is assigned, false otherwise
	 */
	public boolean isUserInGame(final User user) {
		for (Game current : runningGames.values()) {
			if (current.isInGame(user)) {
				return true;
			}
		}

		return false;
	}

	public Game getGameContainingUser(final User user) {
		for (Game current : runningGames.values()) {
			if (current.isInGame(user)) {
				return current;
			}
		}
		return null;
	}

	public class GameNotFoundException extends Exception {

		private static final long serialVersionUID = 1L;

	}

}
