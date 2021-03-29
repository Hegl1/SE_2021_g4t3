package at.qe.timeguess.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.qe.timeguess.dto.Game;
import at.qe.timeguess.dto.RaspberryDiceUpdate;
import at.qe.timeguess.dto.RaspberryRegisterResult;
import at.qe.timeguess.model.RaspberryID;
import at.qe.timeguess.repositories.RaspberryIDRepository;
import at.qe.timeguess.services.RandomStringService;

/**
 * Class that handles communication with raspberries. Also contains a mapping
 * which associates games with raspberry ids.
 */
@RequestMapping("/dice")
@RestController
public class RaspberryController {

	private static final int identifyerLength = 8;

	@Autowired
	private RandomStringService stringGenrator;

	@Autowired
	private RaspberryIDRepository raspbiRepo;

	/**
	 * Map that associates raspberries with games.
	 */
	private Map<String, Game> gameMappings;

	public RaspberryController() {
		this.gameMappings = new HashMap<String, Game>();
	}

	/**
	 * Method that persists a new raspberry with a random id.
	 * 
	 * @return ResponseEntity for REST communication(status 200 if successful).
	 */
	@GetMapping("/register")
	public ResponseEntity<RaspberryRegisterResult> registerRaspberry() {
		String identifier;
		do {
			identifier = stringGenrator.generateRandomString(identifyerLength);
		} while (raspbiRepo.findFirstById(identifier) != null);

		raspbiRepo.save(new RaspberryID(identifier));

		return new ResponseEntity<>(new RaspberryRegisterResult(identifier), HttpStatus.OK);
	}

	/**
	 * Method that receives and updates the dice state in the game that is
	 * registered for the given raspberry id.
	 * 
	 * @param id     id of the raspberry that updates the value.
	 * @param update DTO that contains the new dice side.
	 * @return esponseEntity for REST communication(status 200 if successful, 500 if
	 *         no game is registered)
	 */
	@PostMapping("/{id}/update")
	public ResponseEntity<Void> updateDice(@PathVariable final String id,
			@RequestBody final RaspberryDiceUpdate update) {
		if (update.getSide() < 0 || update.getSide() > 11) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		} else {
			if (!gameMappings.containsKey(id)) {
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			gameMappings.get(id).diceUpdate(update.getSide());
			return new ResponseEntity<>(null, HttpStatus.OK);
		}
	}

	/**
	 * Method that registers a game for a given raspberry id.
	 * 
	 * @param raspbiId id of the raspberry the game should be associated with.
	 * @param game     the game that gets registered.
	 * @return true if successful, false if not.
	 */
	public boolean registerGame(final String raspbiId, final Game game) {
		if (gameMappings.containsKey(raspbiId)) {
			return false;
		} else {
			gameMappings.put(raspbiId, game);
			return true;
		}
	}

	/**
	 * Method to unregister a game of raspberry.
	 * 
	 * @param raspbiId Id of the raspberry that is associated with the game to be
	 *                 unregistered.
	 */
	public void unregisterGame(final String raspbiId) {
		gameMappings.remove(raspbiId);
	}

	public Map<String, Game> getGameMappings() {
		return gameMappings;
	}
}
