package at.qe.timeguess.controllers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.qe.timeguess.controllers.RaspberryController.RaspberryAlreadyInUseException;
import at.qe.timeguess.controllers.RaspberryController.RaspberryNotFoundException;
import at.qe.timeguess.dto.CreateGame;
import at.qe.timeguess.dto.GameDTO;
import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.CategoryRepository;
import at.qe.timeguess.services.LobbyService;
import gamelogic.Dice;
import gamelogic.Game;
import gamelogic.Game.GameCreationException;
import gamelogic.Team;

/**
 * Class that controls creating, viewing and deleting games via REST.
 * 
 *
 */
@RequestMapping("/games")
@RestController
public class GameController {

	@Autowired
	private LobbyService lobbyService;

	// TODO change to service as soon as availalbe
	@Autowired
	private CategoryRepository categoryRepository;

	/**
	 * Method that creates a game from a HTTP request.
	 * 
	 * @param game CreateGame DTO that carries necessary data. Comes form HTTP body.
	 * @return ResponseEntity for REST communication(status 200 if successful, else
	 *         status 403).
	 */
	@PostMapping("")
	public ResponseEntity<Integer> createGame(@RequestBody final CreateGame game) {

		Category gameCateogry = categoryRepository.findFirstById((long) game.getCategory_id());
		try {
			Game newGame;
			if (game.getMapping() == null) {
				newGame = lobbyService.createGame(game.getMax_score(), game.getNumber_of_teams(), gameCateogry,
						game.getDice_code());
			} else {
				newGame = lobbyService.createGame(game.getMax_score(), game.getNumber_of_teams(), gameCateogry,
						buildDice(game), game.getDice_code());
			}

			return new ResponseEntity<Integer>(newGame.getGameCode(), HttpStatus.CREATED);

		} catch (RaspberryAlreadyInUseException e) {
			return new ResponseEntity<Integer>(HttpStatus.FORBIDDEN);
		} catch (GameCreationException e) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		} catch (RaspberryNotFoundException e) {
			return new ResponseEntity<Integer>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Method to fetch all currently running games as an AllGameDTO via REST.
	 * 
	 * @return ResponseEntity for REST communication(status 200 if successful).
	 */
	@GetMapping("")
	public ResponseEntity<List<GameDTO>> getAllRunningGames() {
		Collection<Game> runninGames = lobbyService.getAllRunningGames();
		List<GameDTO> gameDTOs = new LinkedList<>();
		for (Game g : runninGames) {
			gameDTOs.add(buildGameDTO(g));
		}
		return new ResponseEntity<>(gameDTOs, HttpStatus.OK);
	}

	/**
	 * Method to fetch one currently running game by its gamecode as a GameDTO. Code
	 * comes from URL.
	 * 
	 * @param code code of the game to fetch.
	 * @return ResponseEntity for REST communication(status 200 if successful, 404
	 *         if game does not exist).
	 */
	@GetMapping("/{code}")
	public ResponseEntity<GameDTO> getGameInfo(@PathVariable final int code) {
		Game game = lobbyService.getGame(code);
		if (game != null) {

			return new ResponseEntity<GameDTO>(buildGameDTO(game), HttpStatus.OK);
		} else {
			return new ResponseEntity<GameDTO>(HttpStatus.NOT_FOUND);
		}

	}

	/**
	 * Method that forcefully closes a game via HTTP request.
	 * 
	 * @param code the code of the game to close. Comes from the URL.
	 * @return ResponseEntity for REST communication(status 200 if successful, 404
	 *         if game does not exist).
	 */
	@DeleteMapping("/{code}")
	public ResponseEntity<Void> forceCloseRunningGame(@PathVariable final int code) {
		if (lobbyService.getGame(code) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} else {
			lobbyService.deleteRunningGame(code);
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	/**
	 * Private method to build a GameDTO from a game.
	 * 
	 * @param game the game to build the DTO from.
	 * @return the corresponding DTO
	 */
	private GameDTO buildGameDTO(final Game game) {
		List<TeamDTO> teams = new LinkedList<TeamDTO>();
		for (Team t : game.getTeams()) {
			List<UserDTO> users = new LinkedList<UserDTO>();
			for (User u : t.getPlayers()) {
				users.add(buildUserDTO(u));
			}
			teams.add(new TeamDTO(t.getName(), t.getScore(), users));
		}
		return new GameDTO(game.getGameCode(), teams, buildUserDTO(game.getHost()), game.getCategory(),
				game.getMaxPoints());
	}

	/**
	 * Private method to build a Dice from a Mapping DTO.
	 * 
	 * @param game CreateGame DTO which contains the MappingDTO
	 * @return corresponding Dice.
	 */
	private Dice buildDice(final CreateGame game) {
		int[] pointsMapping = new int[12];
		int[] durationMapping = new int[12];
		String[] activityMapping = new String[12];

		for (int i = 0; i < 12; i++) {
			pointsMapping[i] = game.getMapping()[i].getPoints();
			durationMapping[i] = game.getMapping()[i].getTime();
			activityMapping[i] = game.getMapping()[i].getAction();
		}

		return new Dice(pointsMapping, activityMapping, durationMapping);
	}

	/**
	 * Private method that builds a UserDTO from a User.
	 * 
	 * @param user user to build DTO from
	 * @return corresponding UserDTO
	 */
	private UserDTO buildUserDTO(final User user) {
		if (user == null) {
			return null;
		} else {
			return new UserDTO(user.getId(), user.getUsername(), user.getRole().toString().toLowerCase());
		}

	}

}