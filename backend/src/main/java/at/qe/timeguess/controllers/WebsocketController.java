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
import at.qe.timeguess.websockDto.ResponseDTO;

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
			// implement with gameflow (?)
		}
	}

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

	@EventListener
	public void onDisconnectEvent(final SessionDisconnectEvent event) {
		// TODO detect user, do usefull stuff
	}

	@EventListener
	public void onConnectedEvent(final SessionConnectedEvent event) {
		// TODO detect user, do usefull stuff
	}

}
