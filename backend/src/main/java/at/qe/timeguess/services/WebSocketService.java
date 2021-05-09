package at.qe.timeguess.services;

import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.model.User;
import at.qe.timeguess.websockDto.*;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private LobbyService lobbyService;

    private static final String INGAMEQUEUE = "/messagequeue/ingame/";

    private static final String TEAMQUEUE = "/messagequeue/ingame/team/";

    private final Map<String, User> websocketUserMapping = new HashMap<>();
    private final Map<String, WebSocketSession> websocketSessionMapping = new HashMap<>();

    public void addSession(WebSocketSession session) {
        websocketSessionMapping.put(session.getId(), session);
    }

    public void removeSession(String sessionId) {
        websocketSessionMapping.remove(sessionId);
    }

    /**
     * Method that updates the waitingData in frontend.
     *
     * @param id      the gamecode
     * @param content waitingDataDTO
     */
    public void sendWaitingDataToFrontend(final int id, final WaitingDataDTO content) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("READY_UPDATE", content));
    }

    /**
     * Method that notifies content that the game is not continuable.
     *
     * @param id the gamecode
     */
    public void sendGameNotContinuableToFrontend(final int id) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("GAME_NOT_CONTINUEABLE", null));
    }

    /**
     * Method that updates team related information in frontend.
     *
     * @param id    the gamecode
     * @param teams List of teamDTOs to update frontend with.
     */
    public void sendTeamUpdateToFrontend(final int id, final List<TeamDTO> teams) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id,
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
        simpMessagingTemplate.convertAndSend(TEAMQUEUE + id + "/" + teamIndex,
            new ResponseDTO("RUNNING_DATA", content));
    }

    /**
     * Method that sends scoreChanges to frontend
     *
     * @param id     the gamecode
     * @param update ScoreUpdateDTO
     */
    public void sendScoreChangeToFrontend(final int id, final ScoreUpdateDTO update) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("SCORE_UPDATE", update));
    }

    /**
     * Method that sends BatteryUpdates to frontend
     *
     * @param id     the gamecode
     * @param update BatteryUpdateDTO
     */
    public void sendBatteryUpdateToFrontend(final int id, final BatteryUpdateDTO update) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("BAT_UPDATE", update));
    }

    /**
     * Method that sends Dice-Raspberry connection updates to frontend.
     *
     * @param id     the gamecode
     * @param update DiceConnectionUpdateDTO
     */
    public void sendConnectionUpdateToFrontend(final int id, final DiceConnectionUpdateDTO update) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("CONN_UPDATE", update));
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
            simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("EARLY_FINISH", content));
        } else {
            simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("REGULAR_FINISH", content));
        }
    }

    /**
     * Method that sends complete game information to frontend
     *
     * @param id     the gamecode
     * @param update all available information of the game.
     */
    public void sendCompleteGameUpdateToFrontend(final int id, final StateUpdateDTO update) {
        simpMessagingTemplate.convertAndSend(INGAMEQUEUE + id, new ResponseDTO("FULL_INFO", update));
    }

    @EventListener
    public void onDisconnectEvent(final SessionDisconnectEvent event) {
        User u = websocketUserMapping.get(event.getSessionId());
        Game game;

        if (u != null && (game = this.lobbyService.getGameContainingUser(u)) != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (!websocketUserMapping.containsValue(u)) {
                        game.leaveGame(u);
                    }
                } catch (InterruptedException ignored) {
                }
            }).start();
        }

        this.websocketUserMapping.remove(event.getSessionId());
    }

    @EventListener
    public void onConnectedEvent(final SessionConnectedEvent event) {
        String sessionId = null;

        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            sessionId = accessor.getMessageHeaders().get(SimpMessageHeaderAccessor.SESSION_ID_HEADER, String.class);
            GenericMessage<?> generic = (GenericMessage<?>) accessor.getHeader(SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER);
            SimpMessageHeaderAccessor nativeAccessor = SimpMessageHeaderAccessor.wrap(generic);
            List<String> tokenValue = nativeAccessor.getNativeHeader("token");

            if (tokenValue == null) {
                throw new NullPointerException("Token is null");
            }

            String jwtToken = tokenValue.stream().findFirst().orElse(null);

            Claim idClaim = null;

            // Removing Bearer from token
            if (jwtToken != null && jwtToken.startsWith("Bearer")) {
                jwtToken = jwtToken.substring(7);
                idClaim = authenticationService.getClaimFromToken(jwtToken, "user_id");
            }

            //Validate token and set authentication if valid
            if (idClaim != null) {
                Long id = idClaim.asLong();
                User user = this.userService.getUserById(id);

                if (authenticationService.validateToken(jwtToken, user)) {
                    this.websocketUserMapping.put(sessionId, user);

                    this.lobbyService.getGameContainingUser(user).joinGame(user);

                    return;
                }
            }

            throw new AuthenticationServiceException("Bad authentication");
        } catch (Exception e) {
            try {
                this.websocketSessionMapping.get(sessionId).close(CloseStatus.NOT_ACCEPTABLE);
            } catch (IOException ignored) {
            }
        }
    }

    public void setWebsocketControllerForGame(final Game game) {
        game.setWebSocketController(this);
    }

}
