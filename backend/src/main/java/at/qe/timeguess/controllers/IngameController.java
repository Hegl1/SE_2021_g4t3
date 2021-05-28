package at.qe.timeguess.controllers;

import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.gamelogic.Game.HostAlreadyReadyException;
import at.qe.timeguess.gamelogic.Game.TeamIndexOutOfBoundsException;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.UserService;
import at.qe.timeguess.websockDto.JoinOfflineUserDto;
import at.qe.timeguess.websockDto.StateUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/ingame")
@RestController
public class IngameController {

	@Autowired
	private LobbyService lobbyService;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("")
	public ResponseEntity<Boolean> isUserIngame() {

		User authUser = userService.getAuthenticatedUser();
		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} else {
			boolean response = lobbyService.isUserInGame(authUser);
			return new ResponseEntity<Boolean>(response, HttpStatus.OK);
		}
	}

	@PostMapping("/{gameCode}/ready")
	public ResponseEntity<Void> setPlayerReadyState(@PathVariable final int gameCode,
			@RequestBody final boolean update) {

		User authUser = userService.getAuthenticatedUser();
		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		Game game = lobbyService.getGame(gameCode);
		if (game == null || !game.isInGame(authUser)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		game.updateReadyStatus(authUser, update);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/{gameCode}/leave")
	public ResponseEntity<Void> LeaveGame(@PathVariable final int gameCode) {

		User authUser = userService.getAuthenticatedUser();
		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		Game game = lobbyService.getGame(gameCode);
		if (game == null || !game.isInGame(authUser)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		game.leaveGame(authUser);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/{gameCode}/teams/{index}/join")
	public ResponseEntity<Void> joinTeam(@PathVariable final int gameCode, @PathVariable final int index) {

		User authUser = userService.getAuthenticatedUser();
		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		Game game = lobbyService.getGame(gameCode);
		if (game == null || !game.isInGame(authUser)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			game.joinTeam(game.getTeamByIndex(index), authUser);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (HostAlreadyReadyException e) {
			// TODO conflict?
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (TeamIndexOutOfBoundsException e) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	@PostMapping("/{gameCode}/teams/{index}/players")
	public ResponseEntity<Void> addPlayer(@PathVariable final int gameCode, @PathVariable final int index,
			@RequestBody final JoinOfflineUserDto payload) {
		User authUser = userService.getAuthenticatedUser();

		User userToLogin = userService.getUserByUsername(payload.getUsername());
		Game game = lobbyService.getGame(gameCode);

		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		if (userToLogin == null || !passwordEncoder.matches(payload.getPassword(), userToLogin.getPassword())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (game == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (!authUser.equals(game.getHost())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		if (lobbyService.isUserInGame(userToLogin)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		try {
			game.joinTeam(game.getTeamByIndex(index), userToLogin);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (HostAlreadyReadyException e) {
			// Talk to Matthias
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		} catch (TeamIndexOutOfBoundsException e) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	@GetMapping("/state")
	public ResponseEntity<StateUpdateDTO> fetchGameInformation() {
		User authUser = userService.getAuthenticatedUser();

		if (authUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (!lobbyService.isUserInGame(authUser)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Game game = lobbyService.getGameContainingUser(authUser);
        return new ResponseEntity<>(game.getDtoFactory().buildStateUpdate(game), HttpStatus.OK);

    }

    @PostMapping("{gameCode}/confirm")
    public ResponseEntity<Void> confirmGuess(@PathVariable final int gameCode, @RequestBody final String decision) {
        User authUser = userService.getAuthenticatedUser();

        if (authUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Game game = lobbyService.getGame(gameCode);

        if (game == null || !game.isInGame(authUser)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (game.isUserInCurrentTeam(authUser)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        game.confirmExpression(decision);
        return new ResponseEntity<>(HttpStatus.OK);

    }

}
