package at.qe.timeguess.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.qe.timeguess.services.RandomCodeService;
import at.qe.timeguess.services.RaspberryService;

/**
 * Class that handles communication with raspberries. Also contains a mapping
 * which associates games with raspberry ids.
 */
@RequestMapping("/dice")
@RestController
public class RaspberryController {

	private static final int identifyerLength = 8;

	@Autowired
	private RandomCodeService codeGenerator;

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
	 * @return esponseEntity for REST communication(status 200 if successful, 500 if
	 *         no game is registered)
	 */
	@PostMapping("/{id}/update")
	public ResponseEntity<Void> updateDice(@PathVariable final String id, @RequestBody final int update) {
		if (update < 0 || update > 11) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		} else {
			try {
				raspiService.updateDice(id, update);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} catch (at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException e) {
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		}
	}

}
