package at.qe.timeguess.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import at.qe.timeguess.gamelogic.Game.GameNotContinuableException;
import at.qe.timeguess.gamelogic.Game.TeamIndexOutOfBoundsException;
import at.qe.timeguess.gamelogic.Game.UserStateException;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.LobbyService;
import at.qe.timeguess.services.LobbyService.GameNotFoundException;
import at.qe.timeguess.services.UserService;
import at.qe.timeguess.websockDto.ErrorDTO;
import at.qe.timeguess.websockDto.JoinTeamDTO;
import at.qe.timeguess.websockDto.PlayerReadyDTO;
import at.qe.timeguess.websockDto.ResponseDTO;
import at.qe.timeguess.websockDto.UserLeaveDTO;

@Controller
public class WebsocketController {

	@Autowired
	private LobbyService lobbyService;

	@Autowired
	private UserService userService;

	@Autowired
	private SimpMessagingTemplate simpMessageingTemplate;

	private static final String INGAMEQUEUE = "/messagequeue/ingame/";

	private static final String USERQUEUE = "/messagequeue/user/";

	/**
	 * Method for connecting the current user with a device to a game.
	 * 
	 * @param id gamecode
	 */
	@MessageMapping("/joinGame/{id}")
	public void connectAuthUserToGame(@DestinationVariable final Integer id) {
		User player = userService.getAuthenticatedUser();
		try {
			lobbyService.joinGame(id, player);
			simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id,
					new ResponseDTO("LEAVE_TEAM", new JoinTeamDTO(-1, player.getUsername())));
		} catch (GameNotFoundException e) {
			simpMessageingTemplate.convertAndSend(USERQUEUE + player.getId(),
					new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_FOUND")));
		} catch (UserStateException e) {
			// TODO send update specific user
			// TODO send update to host for marking as not movable
			// implement with gameflow (?)
		}
	}

	/**
	 * Method to join a team. If a user is not already in the game, it gets added as
	 * a non device user to the given team.
	 * 
	 * @param id      gamecode
	 * @param payload dto with neccessarry information
	 */
	@MessageMapping("/joinTeam/{id}")
	public void joinTeam(@DestinationVariable final Integer id, @Payload final JoinTeamDTO payload) {

		User player = userService.getUserByUsername(payload.getUserName());
		if (player == null) {
			player = userService.getAuthenticatedUser();
		}
		System.out.println(player.getUsername());
		try {
			lobbyService.joinTeam(id, player, payload.getTeamIndex());
			simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("JOIN_TEAM", payload));
		} catch (GameNotFoundException e) {
			simpMessageingTemplate.convertAndSend(USERQUEUE + player.getId(),
					new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_FOUND")));
		} catch (TeamIndexOutOfBoundsException e) {
			simpMessageingTemplate.convertAndSend(USERQUEUE + player.getId(),
					new ResponseDTO("ERROR", new ErrorDTO("INVALID_TEAM_INDEX")));
		}
	}

	/**
	 * Method to make the current user leave a team.
	 * 
	 * @param id gamecode
	 */
	@MessageMapping("/leaveTeam/{id}")
	public void leaveTeam(@DestinationVariable final Integer id) {
		User player = userService.getAuthenticatedUser();
		System.out.println(player.getUsername());
		try {
			lobbyService.leaveTeam(id, player);
			simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id,
					new ResponseDTO("LEAVE_TEAM", new JoinTeamDTO(-1, player.getUsername())));
		} catch (GameNotFoundException e) {
			simpMessageingTemplate.convertAndSend(USERQUEUE + player.getId(),
					new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_FOUND")));
		}
	}

	/**
	 * Method to make the current user leave the game. Potentially stays in the game
	 * as 'offline player'
	 * 
	 * @param id gamecode
	 */
	@MessageMapping("/leaveGame/{id}")
	public void leaveGame(@DestinationVariable final Integer id) {
		User player = userService.getAuthenticatedUser();
		try {
			lobbyService.leaveGame(id, player);
		} catch (GameNotContinuableException e) {
			lobbyService.abortRunningGame(id);
			simpMessageingTemplate.convertAndSend(USERQUEUE + player.getId(),
					new ResponseDTO("ERROR", new ErrorDTO("GAME_CLOSE")));
		} catch (GameNotFoundException e) {
			simpMessageingTemplate.convertAndSend(USERQUEUE + player.getId(),
					new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_FOUND")));
		}

	}

	/**
	 * Updates a players ready status.
	 * 
	 * @param id          gamecode
	 * @param readyStatus new ready status.
	 */
	@MessageMapping("/readyPlayer/{id}")
	public void updatePlayerReady(@DestinationVariable final Integer id, @Payload final Boolean readyStatus) {
		User player = userService.getAuthenticatedUser();
		lobbyService.updateReadyStatus(id, player, readyStatus);
	}

	/**
	 * Update ready status of a given user in frontend
	 * 
	 * @param id      gamecode
	 * @param content DTO containing neccessarry information
	 */
	public void updateReadyInFrontend(final int id, final PlayerReadyDTO content) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("READY_UPDATE", content));
	}

	/**
	 * Method for communicating that host is already in ready state to frontend.
	 * 
	 * @param userName user to notify
	 */
	public void sendHostIsReadyErrorToFrontend(final String userName) {
		simpMessageingTemplate.convertAndSend(USERQUEUE + userName,
				new ResponseDTO("ERROR", new ErrorDTO("HOST_READY")));
	}

	/**
	 * Method for communicating that the host cannot get ready yet.
	 * 
	 * @param userName name of the host
	 */
	public void sendHostNotReadyableToFrontend(final String userName) {
		simpMessageingTemplate.convertAndSend(USERQUEUE + userName,
				new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_READY")));
	}

	/**
	 * Method for communicating that an unassigned player has left the lobby
	 * 
	 * @param id       gamecode
	 * @param username name of the player that left
	 */
	public void sendPlayerLeftToFrontend(final int id, final String username) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id,
				new ResponseDTO("USER_LEFT", new UserLeaveDTO(username)));
	}

	@EventListener
	public void onDisconnectEvent(final SessionDisconnectEvent event) {
		// TODO detect user, do usefull stuff
	}

	@EventListener
	public void onConnectedEvent(final SessionConnectedEvent event) {
		// TODO detect user, do usefull stuff
	}

}
