package at.qe.timeguess.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import at.qe.timeguess.controllers.RaspberryController;
import at.qe.timeguess.controllers.RaspberryController.RaspberryAlreadyInUseException;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;
import gamelogic.Dice;
import gamelogic.Game;

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
	private RaspberryController raspberryController;

	/**
	 * Map that holds all open games.
	 */
	private Map<Integer, Game> runningGames;

	/**
	 * constant for generating game codes;
	 */
	private static final int MAX_GAME_CODE_LENGTH = 8;

	public LobbyService() {
		this.runningGames = new HashMap<Integer, Game>();
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
	 */
	public Game createGame(final int maxPoints, final int numberOfTeams, final Category category,
			final String raspberryId) throws RaspberryAlreadyInUseException {
		Game newGame = new Game(generateGameCode(), maxPoints, numberOfTeams, category,
				userService.getAuthenticatedUser(), raspberryId);
		runningGames.put(newGame.getGameCode(), newGame);
		raspberryController.registerGame(raspberryId, newGame);
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
	 */
	public Game createGame(final int maxPoints, final int numberOfTeams, final Category category, final Dice dice,
			final String raspberryId) throws RaspberryAlreadyInUseException {
		Game newGame = new Game(generateGameCode(), maxPoints, numberOfTeams, category,
				userService.getAuthenticatedUser(), raspberryId);
		runningGames.put(newGame.getGameCode(), newGame);
		raspberryController.registerGame(raspberryId, newGame);
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
	 */
	public void joinGame(final int gameCode, final User user) {
		if (runningGames.containsKey(gameCode)) {
			runningGames.get(gameCode).joinGame(user);
		}
	}

	/**
	 * Method that forcefully closes a game and removes it from the index.
	 * 
	 * @param gameCode code of the game to close.
	 */
	// TODO check for user user roles
	public void deleteRunningGame(final int gameCode) {
		if (runningGames.containsKey(gameCode)) {
			runningGames.get(gameCode).forceClose();
			closeFinishedGame(gameCode);
		}
	}

	/**
	 * Removes a game from the index. Not to be used to forcefully close a game.
	 * 
	 * @param gameCode
	 */
	public void closeFinishedGame(final int gameCode) {
		if (runningGames.containsKey(gameCode)) {
			raspberryController.unregisterGame(runningGames.get(gameCode).getRaspberryId());
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

}
