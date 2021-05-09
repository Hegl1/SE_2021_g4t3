package at.qe.timeguess.controllers;

import at.qe.timeguess.model.RaspberryID;
import at.qe.timeguess.services.RaspberryService;
import at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Class that handles communication with raspberries. Also contains a mapping
 * which associates games with raspberry ids.
 */
@RequestMapping("/dice")
@RestController
public class RaspberryController {

	@Autowired
	private RaspberryService raspiService;

	/**
	 * Method that persists a new raspberry with a random id.
	 *
	 * @return ResponseEntity for REST communication(status 200 if successful).
	 */
	@GetMapping("/register")
	public ResponseEntity<String> registerRaspberry() {
		String identifier = raspiService.registerRaspberry();
		return new ResponseEntity<>(identifier, HttpStatus.OK);
	}

	/**
	 * Method that receives and updates the dice state in the game that is
	 * registered for the given raspberry id.
	 *
	 * @param id     id of the raspberry that updates the value.
	 * @param update DTO that contains the new dice side.
     * @return ResponseEntity for REST communication(status 200 if successful, 404
     *         if raspberry is not found, 400 if update is out of bounds )
	 */
	@PostMapping("/{id}/update")
	public ResponseEntity<Void> updateDice(@PathVariable final String id, @RequestBody final int update) {
		if (update < 0 || update > 11) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		} else {
			try {
				raspiService.updateDice(id, update);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} catch (RaspberryNotFoundException e) {
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		}
	}

	/**
	 * Method that receives updates for dice battery levels.
	 *
	 * @param id           id of the raspberry that updates its battery level.
	 * @param batteryLevel value of the new battery level
	 * @return ResponseEntity for REST communication(status 200 if successful, 404
	 *         if raspberry not registered)
	 */
	@PostMapping("/{id}/notify/battery")
	public ResponseEntity<Void> updateDiceBattery(@PathVariable final String id, @RequestBody final int batteryLevel) {
		try {
			raspiService.updateDiceBatteryStatus(id, batteryLevel);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (RaspberryNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

    /**
     * Method that receives updates for dice connection statuses.
     *
     * @param id               id of the raspberry that updates its connection
     *                         status.
     * @param connectionStatus new connection status.
     * @return ResponseEntity for REST communication(status 200 if successful, 404
     * if raspberry not registered, 204 if dice is not assigned to a game)
     */
	@PostMapping("/{id}/notify/connection")
	public ResponseEntity<Void> updateDiceConnection(@PathVariable final String id,
			@RequestBody final boolean connectionStatus) {
		try {
			if (raspiService.updateDiceConnectionStatus(id, connectionStatus)) {
				return new ResponseEntity<Void>(HttpStatus.OK);
			} else {
				return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
			}
		} catch (RaspberryNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping("/{id}/available")
	public ResponseEntity<Void> checkDiceAvailability(@PathVariable final String id) {

		RaspberryID raspi = raspiService.getRaspberryById(id);

		if (raspi == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (raspiService.getGameMappings().get(id) != null) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

}
