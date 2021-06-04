package at.qe.timeguess.tests;

import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.gamelogic.Team;
import at.qe.timeguess.model.User;
import at.qe.timeguess.services.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GameTest {

    @Autowired
    WebSocketService webSocketService;

    @Autowired
    ExpressionService expService;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    StatisticsService statsService;

    @Autowired
    CategoryService catService;

    @Autowired
    UserService userService;

    private Game buildTestGame() {
        User host = userService.getUserByUsername("admin");
        Game g;
        try{
            g =  new Game(123456, 6, 2,
                catService.getCategoryByName("Deutschland"), host, "RASPIID");
        } catch(Game.GameCreationException e){
            g = new Game(123456);
        }
        g.setLobbyService(lobbyService);
        g.setStatisticService(statsService);
        g.setWebSocketController(webSocketService);
        g.setExpressionService(expService);
        return g;
    }

    private Game buildStartedGame() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException {

        Game g = buildTestGame();
        User diana = userService.getUserByUsername("diana");
        User thomas = userService.getUserByUsername("thomas");
        User aaron = userService.getUserByUsername("aaron");
        User marcel = userService.getUserByUsername("marcel");
        g.joinGame(diana);
        g.joinTeam(g.getTeamByIndex(1), diana);
        g.joinTeam(g.getTeamByIndex(0), userService.getUserByUsername("admin"));
        g.joinTeam(g.getTeamByIndex(1), aaron);
        g.joinTeam(g.getTeamByIndex(0), thomas);
        g.updateReadyStatus(userService.getUserByUsername("admin"), true);
        g.updateReadyStatus(diana, true);
        return g;
    }

    @Test
    public void testGameCreationFailure(){
        Assertions.assertThrows(Game.GameCreationException.class, () -> new Game(123, 2, 1, null, new User(), "RASPPID"));
    }


    @Test
    public void testGameCreationSuccess(){
        Game g = buildTestGame();
        Assertions.assertEquals(2, g.getTeams().size());
        Assertions.assertEquals("admin",g.getHost().getUsername());
        Assertions.assertEquals(6, g.getMaxPoints());
    }

    @Test
    public void testJoinGame() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException {

        Game g = buildTestGame();
        g.setActive(true);
        Assertions.assertThrows(Game.GameAlreadyRunningException.class, () -> g.joinGame(new User()));
        g.setActive(false);
        g.joinGame(userService.getUserByUsername("diana"));
        Assertions.assertEquals(2, g.getUnassignedUsers().size());
        g.joinTeam(g.getTeamByIndex(0),userService.getUserByUsername("thomas"));
        g.joinGame(userService.getUserByUsername("thomas"));
        Assertions.assertEquals(3, g.getUsersWithDevices().size());
    }

    @Test
    public void testJoinTeam() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException {

        Game g = buildTestGame();
        User diana = userService.getUserByUsername("diana");
        User thomas = userService.getUserByUsername("thomas");
        User aaron = userService.getUserByUsername("aaron");
        User marcel = userService.getUserByUsername("marcel");
        g.joinGame(diana);
        g.joinTeam(g.getTeamByIndex(1), diana);
        Assertions.assertEquals(1, g.getTeamByIndex(1).getPlayers().size());
        g.joinTeam(g.getTeamByIndex(0), userService.getUserByUsername("admin"));
        g.joinTeam(g.getTeamByIndex(1), aaron);
        g.joinTeam(g.getTeamByIndex(0), thomas);
        g.updateReadyStatus(userService.getUserByUsername("admin"), true);
        Assertions.assertThrows(Game.HostAlreadyReadyException.class, () -> g.joinTeam(g.getTeamByIndex(0), marcel));
    }

    @Test
    public void testLeaveGame() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException {
        Game g = buildTestGame();
        User diana = userService.getUserByUsername("diana");
        g.joinGame(diana);
        g.joinTeam(g.getTeamByIndex(0), diana);
        g.leaveGame(diana);
        Assertions.assertFalse(g.isInGame(diana));
    }

    @Test
    public void testUpdateReadyStatus() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException {

        Game g = buildTestGame();
        User diana = userService.getUserByUsername("diana");
        g.joinGame(diana);
        g.joinTeam(g.getTeamByIndex(0), diana);
        g.updateReadyStatus(diana, true);
        Assertions.assertTrue(g.getReadyPlayers().get(diana));
    }

    @Test
    public void testStartGame() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException {
        Game g = buildStartedGame();
        Assertions.assertTrue(g.isActive());
    }

    @Test
    public void testDiceUpdate() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException{

        Game g = buildStartedGame();
        Assertions.assertEquals(-1, g.getRunningGamePhase().getRoundStartTime());
        Assertions.assertEquals(-1, g.getRunningGamePhase().getRoundEndTime());

        g.diceUpdate(2);
        Assertions.assertNotEquals(-1, g.getRunningGamePhase().getGameStartTime());
        g.diceUpdate(3);
        Assertions.assertNotEquals(-1, g.getRunningGamePhase().getRoundEndTime());
    }

    @Test
    public void testConfirmExpression() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException{

        Game g = buildStartedGame();
        g.diceUpdate(2);
        g.confirmExpression("WRONG");
        g.diceUpdate(3);
        g.confirmExpression("INVALID");
        for (Team t : g.getTeams()) {
            Assertions.assertEquals(0, t.getScore());
        }
        g.diceUpdate(2);
        g.confirmExpression("CORRECT");
        int max = 0;
        for (Team t : g.getTeams()) {
            if (t.getScore() >= max){
                max = t.getScore();
            }
        }
        Assertions.assertTrue(max > 0);
        Assertions.assertTrue(g.getRunningGamePhase().getRoundCounter() > 0);
    }

    @Test
    public void testUpdateDiceBattery() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException{

        Game g = buildStartedGame();
        g.updateDiceBattery(20);
        Assertions.assertEquals(20, g.getDice().getBatteryPower());
    }

    @Test
    public void testUpdateDiceConnection() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException{

        Game g = buildStartedGame();
        g.updateDiceConnection(false);
        Assertions.assertFalse(g.getDice().isRaspberryConnected());
        g.confirmExpression("CORRECT");
        for (Team t : g.getTeams()) {
            Assertions.assertEquals(0, t.getScore());
        }
    }

    @Test
    public void testIsInGame() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException{

        Game g = buildStartedGame();
        User matthias = userService.getUserByUsername("matthias");
        User diana = userService.getUserByUsername("diana");

        Assertions.assertFalse(g.isInGame(matthias));
        Assertions.assertTrue(g.isInGame(diana));

    }

    @Test
    public void testIsUserInCurrentTeam() throws Game.GameAlreadyRunningException,
        Game.TeamIndexOutOfBoundsException, Game.HostAlreadyReadyException{

        Game g = buildStartedGame();
        User currentUser = g.getTeamByIndex(g.getCurrentTeam()).getCurrentPlayer();
        Assertions.assertTrue(g.isUserInCurrentTeam(currentUser));
    }

}
