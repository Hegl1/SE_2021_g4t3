package at.qe.timeguess.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.websockDto.BatteryUpdateDTO;
import at.qe.timeguess.websockDto.DiceConnectionUpdateDTO;
import at.qe.timeguess.websockDto.FinishedGameDTO;
import at.qe.timeguess.websockDto.ResponseDTO;
import at.qe.timeguess.websockDto.RunningDataDTO;
import at.qe.timeguess.websockDto.ScoreUpdateDTO;
import at.qe.timeguess.websockDto.StateUpdateDTO;
import at.qe.timeguess.websockDto.TeamResponseDTO;
import at.qe.timeguess.websockDto.WaitingDataDTO;

@Controller
public class WebSocketService {

	@Autowired
	private SimpMessagingTemplate simpMessageingTemplate;

	private static final String INGAMEQUEUE = "/messagequeue/ingame/";

	private static final String TEAMQUEUE = "/messagequeue/ingame/team/";

	/**
	 * Method that updates the waitingData in frontend.
	 * 
	 * @param id      the gamecode
	 * @param content waitingDataDTO
	 */
	public void sendWaitingDataToFrontend(final int id, final WaitingDataDTO content) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("READY_UPDATE", content));
	}

	/**
	 * Method that notifies content that the game is not continueable.
	 * 
	 * @param id the gamecode
	 */
	public void sendGameNotContinueableToFrontend(final int id) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("GAME_NOT_CONTINUEABLE", null));
	}

	/**
	 * Method that updates team related information in frontend.
	 * 
	 * @param id    the gamecode
	 * @param teams List of teamDTOs to update frontend with.
	 */
	public void sendTeamUpdateToFrontend(final int id, final List<TeamDTO> teams) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id,
				new ResponseDTO("TEAM_UPDATE", new TeamResponseDTO(teams)));
	}

	/**
	 * Method that sends runningData information to a given team in a given game.
	 * 
	 * @param id        gamecode
	 * @param teamIndex index of the team in game
	 * @param content   RunningDataDTO containgn updates
	 */
	public void sendRunningDataToTeam(final int id, final int teamIndex, final RunningDataDTO content) {
		simpMessageingTemplate.convertAndSend(TEAMQUEUE + id + "/" + teamIndex,
				new ResponseDTO("RUNNING_DATA", content));
	}

	/**
	 * Method that sends scoreChanges to frontend
	 * 
	 * @param id     the gamecode
	 * @param update ScoreUpdateDTO
	 */
	public void sendScoreChangeToFrontend(final int id, final ScoreUpdateDTO update) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("SCORE_UPDATE", update));
	}

	/**
	 * Method that sends BatteryUpdates to frontend
	 * 
	 * @param id     the gamecode
	 * @param update BatteryUpdateDTO
	 */
	public void sendBatteryUpdateToFrontend(final int id, final BatteryUpdateDTO update) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("BAT_UPDATE", update));
	}

	/**
	 * Method taht sends Dice-Raspberry connection updates to frontend.
	 * 
	 * @param id     the gamecode
	 * @param update DiceConnectionUpdateDTO
	 */
	public void sendConnectionUpdateToFrontend(final int id, final DiceConnectionUpdateDTO update) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("CONN_UPDATE", update));
	}

	/**
	 * Method that sends finished game information to frontend. Distinguish between
	 * early finishes due to lack of expressions and regular finishes
	 * 
	 * @param id          the gamecode
	 * @param content     FinishedGameDTO
	 * @param earlyFinish true when game finished early, else false
	 */
	public void sendFinishGameToFrontend(final int id, final FinishedGameDTO content, final boolean earlyFinish) {
		if (earlyFinish) {
			simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("EARLY_FINISH", content));
		} else {
			simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("REGULAR_FINISH", content));
		}
	}

	/**
	 * Method that sends complete game information to fronend
	 * 
	 * @param id     the gamecode
	 * @param update all available information of the game.
	 */
	public void sendCompleteGameUpdateToFrontend(final int id, final StateUpdateDTO update) {
		simpMessageingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("FULL_INFO", update));
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
