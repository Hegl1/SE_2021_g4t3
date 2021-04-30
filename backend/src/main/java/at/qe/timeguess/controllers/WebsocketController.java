package at.qe.timeguess.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.gamelogic.Team;
import at.qe.timeguess.websockDto.ErrorDTO;
import at.qe.timeguess.websockDto.ResponseDTO;
import at.qe.timeguess.websockDto.TeamResponseDTO;
import at.qe.timeguess.websockDto.WaitingDataDTO;

@Controller
public class WebsocketController {

	@Autowired
	private SimpMessagingTemplate simpMessageingTemplate;

	private static final String INGAMEQUEUE = "/messagequeue/ingame/";

	private static final String USERQUEUE = "/messagequeue/user/";

	/**
	 * Update ready status of a given user in frontend
	 * 
	 * @param id      gamecode
	 * @param content DTO containing neccessarry information
	 */
	public void updateReadyInFrontend(final int id, final WaitingDataDTO content) {
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
		// TODO maybe delete
		simpMessageingTemplate.convertAndSend(USERQUEUE + userName,
				new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_READY")));
	}

	public void sendGameNotContinueableToFrontend(final int id) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id,
				new ResponseDTO("ERROR", new ErrorDTO("GAME_NOT_CONTINUEABLE")));
	}

	public void sendTeamUpdateToFrontend(final int id, final List<Team> teams) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id,
				new ResponseDTO("TEAM_UPDATE", new TeamResponseDTO(teams)));
	}

	@EventListener
	public void onDisconnectEvent(final SessionDisconnectEvent event) {
		// TODO detect user, do usefull stuff
	}

	@EventListener
	public void onConnectedEvent(final SessionConnectedEvent event) {
		// TODO detect user, do usefull stuff
	}

	public void setWebsocketControllerForGame(final Game game) {
		game.setWebSocketController(this);
	}

}
