package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.Mapping;
import at.qe.timeguess.dto.*;
import at.qe.timeguess.gamelogic.Dice;
import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.gamelogic.Game.GameAlreadyRunningException;
import at.qe.timeguess.gamelogic.Game.GameCreationException;
import at.qe.timeguess.gamelogic.Team;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.CategoryRepository;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.LobbyService.GameNotFoundException;
import at.qe.timeguess.services.RaspberryService.RaspberryNotFoundException;
import at.qe.timeguess.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

	@Autowired
	private UserService userService;

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

        Category gameCategory = categoryRepository.findFirstById((long) game.getCategory_id());
        if (gameCategory == null || !gameCreationParametersInBounds(game)) {
            return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
        }
        try {
            Game newGame;
            if (game.getMapping() == null) {
                newGame = lobbyService.createGame(game.getMax_score(), game.getNumber_of_teams(), gameCategory,
                    game.getDice_code());
            } else {
                newGame = lobbyService.createGame(game.getMax_score(), game.getNumber_of_teams(), gameCategory,
                    buildDice(game), game.getDice_code());
            }

            return new ResponseEntity<Integer>(newGame.getGameCode(), HttpStatus.CREATED);

        } catch (at.qe.timeguess.services.RaspberryService.RaspberryAlreadyInUseException e) {
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
        Collection<Game> runningGames = lobbyService.getAllRunningGames();
        List<GameDTO> gameDTOs = new LinkedList<>();
        for (Game g : runningGames) {
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
			lobbyService.abortRunningGame(code);
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	@PostMapping("/{code}/join")
	public ResponseEntity<Void> joinGame(@PathVariable final int code) {

		User authUser = userService.getAuthenticatedUser();

		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		if (lobbyService.getGameContainingUser(authUser) != null
				&& lobbyService.getGameContainingUser(authUser) != lobbyService.getGame(code)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		try {
			lobbyService.joinGame(code, authUser);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (GameNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (GameAlreadyRunningException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/{code}/exists")
	public ResponseEntity<Void> gameExists(@PathVariable final int code) {

		if (userService.getAuthenticatedUser() == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		if (lobbyService.getGame(code) != null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
			teams.add(new TeamDTO(t.getName(), t.getScore(), users, t.getIndex()));
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

    /**
     * Method that checks whether the numerical parameters for a game creation are in bounds
     *
     * @param createGame createGameDTO that contains the information for the new game.
     * @return true if all params are in bound, else false
     */
    private boolean gameCreationParametersInBounds(CreateGame createGame) {
        final int gameScoreCeiling = 10000;
        final int teamCeiling = 10;
        final int guessTimeCeiling = 600;
        final int guessScoreCeiling = 100;

        if (createGame.getNumber_of_teams() > teamCeiling || createGame.getNumber_of_teams() <= 1 ||
            createGame.getMax_score() > gameScoreCeiling || createGame.getMax_score() <= 0) {
            return false;
        }
        if (createGame.getMapping() != null) {
            for (Mapping mapping : createGame.getMapping()) {
                if (mapping.getTime() > guessTimeCeiling || mapping.getTime() <= 0
                    || mapping.getPoints() > guessScoreCeiling || mapping.getPoints() <= 0) {
                    return false;
                }
            }
        }

        return true;
    }

}
