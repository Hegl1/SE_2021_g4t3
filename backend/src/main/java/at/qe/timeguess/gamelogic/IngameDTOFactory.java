package at.qe.timeguess.gamelogic;

import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.User;
import at.qe.timeguess.websockDto.DiceInfoDTO;
import at.qe.timeguess.websockDto.RunningDataDTO;
import at.qe.timeguess.websockDto.StateUpdateDTO;
import at.qe.timeguess.websockDto.WaitingDataDTO;

import java.util.LinkedList;
import java.util.List;

public class IngameDTOFactory {

    /**
     * Method that builds a List of UserDTOs from a List of Users
     *
     * @param users users to build DTOs from
     * @return a List of UserDTOs
     */
    protected List<UserDTO> buildUserDTOs(final List<User> users) {
        final List<UserDTO> result = new LinkedList<>();
        for (final User u : users) {
            result.add(new UserDTO(u.getId(), u.getUsername(), u.getRole().toString()));
        }
        return result;
    }

    /**
     * Builds a List of all ready players
     *
     * @return List of ready players.
     */
    protected List<User> buildReadyPlayerList(Game game) {
        final List<User> result = new LinkedList<>();
        for (final User current : game.getReadyPlayers().keySet()) {
            if (game.getReadyPlayers().get(current)) {
                result.add(current);
            }
        }
        return result;
    }

    /**
     * Builds a WaitingDataDTO from current game information.
     *
     * @return a correct WaitingDataDTO.
     */
    protected WaitingDataDTO buildWaitingDataDTO(Game game) {
        return new WaitingDataDTO(game.getUnassignedUsers(), buildReadyPlayerList(game), game.checkGameStartable());
    }

    /**
     * Builds a RunningDataDTO from current game information. If isCurrentTeam is
     * true, category is omitted.
     *
     * @param isCurrentTeam true when building for currently guessing team, else
     *                      false.
     * @return a correct RunningDataDTO
     */
    protected RunningDataDTO buildRunningDataDTO(Game game, final boolean isCurrentTeam) {
        if (isCurrentTeam) {
            return new RunningDataDTO(game.getRoundCounter() / game.getNumberOfTeams(), game.getRoundEndTime(), game.getRoundStartTime(), game.getCurrentTeam(), game.getCurrentPlayer(), null,
                game.getDice().getPoints(game.getCurrentFacet()), game.getDice().getDurationInSeconds(game.getCurrentFacet()),
                game.getDice().getActivity(game.getCurrentFacet()));
        } else {
            return new RunningDataDTO(game.getRoundCounter() / game.getNumberOfTeams(), game.getRoundEndTime(), game.getRoundStartTime(), game.getCurrentTeam(), game.getCurrentPlayer(),
                game.getCurrentExpression().getName(), game.getDice().getPoints(game.getCurrentFacet()), game.getDice().getDurationInSeconds(game.getCurrentFacet()),
                game.getDice().getActivity(game.getCurrentFacet()));
        }
    }

    /**
     * Method that builds a StateUpdateDTO from current game information. If game
     * is active, waiting information gets omitted and vice versa.
     *
     * @return a correct StateUpdateDTO
     */
    public StateUpdateDTO buildStateUpdate(Game game) {

        if (game.isActive()) {
            final RunningDataDTO runningData = buildRunningDataDTO(game, false);
            return new StateUpdateDTO("RUNNING", null, runningData, new DiceInfoDTO(game.getDice().isRaspberryConnected(),
                game.getDice().getBatteryPower()), game.getGameCode(), buildTeamDTOs(game.getTeams()), game.getHost(), game.getCategory(),
                game.getMaxPoints());
        } else {
            final WaitingDataDTO waitingData = buildWaitingDataDTO(game);
            return new StateUpdateDTO("WAITING", waitingData, null, new DiceInfoDTO(game.getDice().isRaspberryConnected(),
                game.getDice().getBatteryPower()), game.getGameCode(), buildTeamDTOs(game.getTeams()), game.getHost(), game.getCategory(),
                game.getMaxPoints());
        }

    }

    /**
     * Method that builds a List of TeamDTOs from a List of teams.
     *
     * @param teams teams to build DTOs from
     * @return a List of TeamDTOs
     */
    protected List<TeamDTO> buildTeamDTOs(final List<Team> teams) {
        final List<TeamDTO> result = new LinkedList<>();
        for (final Team t : teams) {
            result.add(new TeamDTO(t.getName(), t.getScore(), buildUserDTOs(t.getPlayers()), t.getIndex()));
        }
        return result;
    }

}
