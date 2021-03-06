package at.qe.timeguess.gamelogic;

import at.qe.timeguess.model.Expression;
import at.qe.timeguess.model.User;
import at.qe.timeguess.websockDto.BatteryUpdateDTO;
import at.qe.timeguess.websockDto.DiceConnectionUpdateDTO;
import at.qe.timeguess.websockDto.FinishedGameDTO;
import at.qe.timeguess.websockDto.ScoreUpdateDTO;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class RunningGamePhase {

    private Game game;
    private int currentTeam;
    private int roundCounter;
    private Integer currentFacet;
    private Set<Long> usedExpressions;
    private Expression currentExpression;
    private Long roundStartTime;
    private Long roundEndTime;
    private long gameStartTime;
    private boolean expressionConfirmed;

    protected  RunningGamePhase(Game game){
        this.usedExpressions = new TreeSet<>();
        this.game = game;
    }

    /**
     * Method that starts the game and sets it up for the first round.
     */
    protected void startGame() {
        game.setActive(true);
        expressionConfirmed = false;
        currentFacet = null;
        gameStartTime = System.currentTimeMillis() / 1000L;
        currentTeam = new Random().nextInt(game.getNumberOfTeams());
        roundCounter = 1;
        if (!pickNewExpression()) {
            finishGame(true);
        }
        roundStartTime = -1L;
        roundEndTime = -1L;
        game.getWebSocketService().sendCompleteGameUpdateToFrontend(game.getGameCode(), game.getDtoFactory().buildStateUpdate(game));
    }

    /**
     * Method that handles incoming facet updates from the rasperry.
     * @param facet the new facet received by the raspberry.
     */
    protected void diceUpdate(final int facet) {
        if (roundStartTime == -1 && game.getDice().isRaspberryConnected()) {
            // between round phase - start timer
            roundStartTime = System.currentTimeMillis() / 1000L;
            currentFacet = facet;
            sendRunningDataToTeams();

        } else {
            if (roundStartTime != -1L && roundEndTime == -1L && game.getDice().isRaspberryConnected()) {
                roundEndTime = System.currentTimeMillis() / 1000L;
                sendRunningDataToTeams();
            }
        }
    }


    /**
     * Method that contains the main logic for deciding whether a player guess the expression correctly and how many
     * points are added to the team scores.
     * @param decision decision, whether an expression got guessed correctly. Has one of the three values: CORRECT, INVALID or WRONG
     */
    protected synchronized void confirmExpression(final String decision) {
        if (roundStartTime != -1 && !expressionConfirmed && game.getDice().isRaspberryConnected()) {
            expressionConfirmed = true;
            if (decision.equals("CORRECT")) {
                correctGuessAction();
                if (game.getTeams().get(currentTeam).getScore() >= game.getMaxPoints()) {
                    finishGame(false);
                    return;
                }
            } else if (decision.equals("INVALID")) {
                invalidGuessAction();
            } else {
                wrongGuessAction();
            }
            start_new_round();
        }
    }

    /**
     * Method that gets called when a guess is correct. Adds points and notifies players accordingly
     */
    private void correctGuessAction(){
        game.getTeams().get(currentTeam).incrementScore(game.getDice().getPoints(currentFacet));
        game.getTeams().get(currentTeam).incrementCorrectExpressions();
        game.getWebSocketService().sendScoreChangeToFrontend(game.getGameCode(),
            new ScoreUpdateDTO(currentTeam, game.getTeams().get(currentTeam).getScore()));
    }

    /**
     * Method that gets called when a guess is invalid. Adds points and notifies players accordingly
     */
    private void invalidGuessAction(){
        game.getTeams().get(currentTeam).decrementScore(1);
        game.getTeams().get(currentTeam).incrementWrongExpressions();
        game.getWebSocketService().sendScoreChangeToFrontend(game.getGameCode(),
            new ScoreUpdateDTO(currentTeam, game.getTeams().get(currentTeam).getScore()));
    }

    /**
     * Method that gets called when a guess is wrong. Adds points and notifies players accordingly
     */
    private void wrongGuessAction(){
        game.getTeams().get(currentTeam).incrementWrongExpressions();
    }

    /**
     * Method that starts a new round after a guess was made.
     */
    private void start_new_round(){
        game.getTeams().get(currentTeam).incrementCurrentPlayer();
        incrementCurrentTeam();
        if (!pickNewExpression()) {
            finishGame(true);
        }
        currentFacet = null;
        roundStartTime = -1L;
        roundEndTime = -1L;
        expressionConfirmed = false;
        roundCounter++;
        sendRunningDataToTeams();
    }

    /**
     * Method that finishes the game properly. Users get notified of an early end if
     * no expressions are left in the category.
     */
    protected void finishGame(final boolean earlyFinish) {
        final long endTime = System.currentTimeMillis() / 1000L;
        game.setActive(false);
        Collections.sort(game.getTeams(), (o1, o2) -> {
            if (o1.getScore() < o2.getScore()) {
                return 1;
            } else if (o1.getScore() == o2.getScore()) {
                return 0;
            } else {
                return -1;
            }
        });

        final FinishedGameDTO finishedGame = new FinishedGameDTO(game.getDtoFactory().buildTeamDTOs(game.getTeams()), roundCounter / game.getNumberOfTeams(),
            getTotalNumberOfCorrectExpressions(), getTotalNumberOfWrongExpressions(), endTime - gameStartTime);

        game.getWebSocketService().sendFinishGameToFrontend(game.getGameCode(), finishedGame, earlyFinish);
        game.persistFinishedGame();
        game.getLobbyService().closeFinishedGame(game.getGameCode());
    }

    /**
     * Method that updates the dices battery status
     */
    protected void updateDiceBattery(final int batteryStatus) {
        game.getDice().setBatteryPower(batteryStatus);
        game.getWebSocketService().sendBatteryUpdateToFrontend(game.getGameCode(), new BatteryUpdateDTO(batteryStatus));
    }

    /**
     * Method that updates the dices connection status. Blocks active game flow if
     * dice is disconnected and starts a fresh round with a fresh expression upon
     * reconnection.
     */
    protected void updateDiceConnection(final boolean isConnected) {

        currentFacet = null;
        roundStartTime = -1L;
        roundEndTime = -1L;

        game.getWebSocketService().sendConnectionUpdateToFrontend(game.getGameCode(), new DiceConnectionUpdateDTO(isConnected));
        game.getDice().setRaspberryConnected(isConnected);

        if (isConnected) {
            if (!pickNewExpression()) {
                finishGame(true);
            }
            expressionConfirmed = false;
        }
        sendRunningDataToTeams();
    }

    /**
     * Method that calculates the total number of correct expressions.
     *
     * @return total number of correct expressions.
     */
    private int getTotalNumberOfCorrectExpressions() {
        int result = 0;
        for (final Team t : game.getTeams()) {
            result += t.getNumberOfCorrectExpressions();
        }
        return result;
    }

    /**
     * Method that calculates the total number of wrong expressions.
     *
     * @return total number of wrong expressions.
     */
    private int getTotalNumberOfWrongExpressions() {
        int result = 0;
        for (final Team t : game.getTeams()) {
            result += t.getNumberOfCorrectExpressions();
        }
        return result;
    }

    /**
     * Method that sends running game information to the teams. The currently
     * guessing team does not get information about the current expression;
     */
    private void sendRunningDataToTeams() {
        for (final Team t : game.getTeams()) {
            if (currentTeam == t.getIndex()) {
                game.getWebSocketService().sendRunningDataToTeam(game.getGameCode(), t.getIndex(), game.getDtoFactory().buildRunningDataDTO(game, true));
            } else {
                game.getWebSocketService().sendRunningDataToTeam(game.getGameCode(), t.getIndex(), game.getDtoFactory().buildRunningDataDTO(game, false));
            }
        }
    }

    /**
     * Method that picks a new random expression and adds it to the usedExpressions
     * list.
     *
     * @return true if expression could be found, false if no expressions are left.
     */
    private boolean pickNewExpression() {
        if (usedExpressions.size() == game.getExpressionService().getAllExpressionsByCategory(game.getCategory()).size()) {
            return false;
        } else {
            do {
                currentExpression = game.getExpressionService().getRandomExpressionByCategory(game.getCategory());
            } while (!usedExpressions.add(currentExpression.getId()));
            return true;
        }
    }

    /**
     * Method that correctly increments the current team.
     */
    private void incrementCurrentTeam() {
        currentTeam = (currentTeam + 1) % game.getTeams().size();
    }

    /**
     * Method that checks whether a user is in the currently guessing team.
     *
     * @param user user to check for
     * @return true if user is in the currently guessing team, else false.
     */
    protected boolean isUserInCurrentTeam(final User user) {
        return game.getTeams().get(currentTeam).getPlayers().contains(user);
    }

    public User getCurrentPlayer() {
        return game.getTeams().get(currentTeam).getCurrentPlayer();
    }

    public int getCurrentTeam() {
        return currentTeam;
    }

    public int getRoundCounter() {
        return roundCounter;
    }

    public Expression getCurrentExpression() {
        return currentExpression;
    }

    public Integer getCurrentFacet() {
        return currentFacet;
    }

    public Long getRoundStartTime() {
        return roundStartTime;
    }

    public Long getRoundEndTime() {
        return roundEndTime;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

}
