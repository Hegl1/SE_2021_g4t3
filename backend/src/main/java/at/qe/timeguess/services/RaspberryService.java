package at.qe.timeguess.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import at.qe.timeguess.model.RaspberryID;
import at.qe.timeguess.repositories.RaspberryIDRepository;
import gamelogic.Game;

@Service
@Scope("application")
public class RaspberryService {

	private static final int identifyerLength = 8;

	@Autowired
	private RandomCodeService codeGenerator;

	@Autowired
	private RaspberryIDRepository raspbiRepo;

	/**
	 * Map that associates raspberries with games.
	 */
	private Map<String, Game> gameMappings;

	public RaspberryService() {
		this.gameMappings = new HashMap<String, Game>();
	}

	/**
	 * Creates a random ID for a new raspberry and saves it in the database.
	 * 
	 * @return the created ID.
	 */
	public String registerRaspberry() {
		String identifier;
		do {
			identifier = codeGenerator.generateRandomRaspberryCode(identifyerLength);
		} while (raspbiRepo.findFirstById(identifier) != null);

		raspbiRepo.save(new RaspberryID(identifier));
		return identifier;

	}

	/**
	 * Calls the diceUpdate method on the game that is registered for the given
	 * raspberry. If the raspberry is not registered to a game, nothing happens.
	 * 
	 * @param raspberryId the raspberry which sends the update
	 * @param update      the facet number of the update
	 * @throws RaspberryNotFoundException if the raspberry does not exist.
	 */
	public void updateDice(final String raspberryId, final int update) throws RaspberryNotFoundException {
		if (raspbiRepo.findFirstById(raspberryId) == null) {
			throw new RaspberryNotFoundException();
		} else {
			if (gameMappings.get(raspberryId) != null) {
				gameMappings.get(raspberryId).diceUpdate(update);
			}
		}
	}

	/**
	 * Method that registers a game for a given raspberry id.
	 * 
	 * @param raspbiId id of the raspberry the game should be associated with.
	 * @param game     the game that gets registered.
	 * @throws RaspberryAlreadyInUseException when raspberry is already assigned to
	 *                                        a running game.
	 */
	public void registerGame(final String raspberryID, final Game game)
			throws RaspberryAlreadyInUseException, RaspberryNotFoundException {
		if (raspbiRepo.findFirstById(raspberryID) == null) {
			throw new RaspberryNotFoundException();
		}

		if (gameMappings.containsKey(raspberryID)) {
			throw new RaspberryAlreadyInUseException("Id " + raspberryID + "already in use");
		} else {
			gameMappings.put(raspberryID, game);
		}
	}

	/**
	 * Method to unregister a game of raspberry.
	 * 
	 * @param raspbiId Id of the raspberry that is associated with the game to be
	 *                 unregistered.
	 */
	public void unregisterGame(final String raspberryID) {
		gameMappings.remove(raspberryID);
	}

	public Map<String, Game> getGameMappings() {
		return this.gameMappings;
	}

	public class RaspberryAlreadyInUseException extends Exception {

		private static final long serialVersionUID = 1L;

		public RaspberryAlreadyInUseException(final String message) {
			super(message);
		}
	}

	public class RaspberryNotFoundException extends Exception {

		private static final long serialVersionUID = 1L;

	}

}
