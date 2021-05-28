package at.qe.timeguess.gamelogic;

import at.qe.timeguess.model.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SetupGamePhase {

    private Game game;
    private Map<User, Boolean> readyPlayers;
    private List<User> unassignedUsers;

    protected SetupGamePhase(Game game) {
        this.game = game;
        this.readyPlayers = new HashMap<>();
        this.unassignedUsers = new LinkedList<>();
        readyPlayers.put(game.getHost(), false);
        this.unassignedUsers.add(game.getHost());
    }


    protected void joinNewUserToGame(User player){
        game.getUsersWithDevices().add(player);
        unassignedUsers.add(player);
        game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
        addToReadyMapIfNotAlreadyExists(player, false);
    }

    protected void promoteNoDeviceUserToDeviceUSer(User player){
        readyPlayers.put(player, false);
        new Thread(() -> {
            try {
                //dirty workaround
                Thread.sleep(100);
                game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
            } catch (InterruptedException ignored) {
            }
        }).start();
        // webSocketService.sendWaitingDataToFrontend(gameCode, buildWaitingDataDTO());
    }


    /**
     * Method that adds a player to the ready map if he is not already in.
     *
     * @param player      player to add
     * @param readyStatus status to add the player with
     */
    protected void addToReadyMapIfNotAlreadyExists(final User player, final boolean readyStatus) {
        if (!readyPlayers.containsKey(player)) {
            readyPlayers.put(player, readyStatus);
            game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
        }
    }

    /**
     * Method to assign a player to a team. If the player is in the unassignedUsers
     * list or in another team, it gets removed from there.
     *
     * @param team   Team to move the user to
     * @param player User to move
     * @throws Game.HostAlreadyReadyException
     */
    protected void joinTeam(final Team team, final User player) throws Game.HostAlreadyReadyException {
        if (!readyPlayers.get(game.getHost())) {
            if (readyPlayers.get(player) != null && readyPlayers.get(player)) {
                // if player is ready, no switch
                return;
            }

            if (team == null) {
                leaveTeam(player);
                game.getWebSocketService().sendTeamUpdateToFrontend(game.getGameCode(), game.getDtoFactory().buildTeamDTOs(game.getTeams()));
                game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
            } else {
                leaveTeam(player);
                unassignedUsers.remove(player);
                // add to ready map if offline player
                addToReadyMapIfNotAlreadyExists(player, true);
                team.joinTeam(player);
                game.getWebSocketService().sendTeamUpdateToFrontend(game.getGameCode(), game.getDtoFactory().buildTeamDTOs(game.getTeams()));
                game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
            }
        } else {
            throw new Game.HostAlreadyReadyException("Host is already ready");
        }

    }

    /**
     * Method to make a player leave a team and add it to the unassigned list.
     *
     * @param player player to unassign.
     * @throws Game.HostAlreadyReadyException
     */
    protected void leaveTeam(final User player) throws Game.HostAlreadyReadyException {
        if (!readyPlayers.get(game.getHost())) {
            for (final Team current : game.getTeams()) {
                if (current.isInTeam(player)) {
                    unassignedUsers.add(player);
                    current.leaveTeam(player);
                    break;
                }
            }
        } else {
            throw new Game.HostAlreadyReadyException("Host is already ready");
        }
    }

    protected void leaveNotStartedGame(final User player){
        for (Team current : game.getTeams()) {
            if (current.isInTeam(player)) {
                current.leaveTeam(player);
                break;
            }
        }
        unassignedUsers.remove(player);
        readyPlayers.remove(player);
        updateReadyStatus(game.getHost(), false);

    }

    protected void updateReadyStatus(final User user, final Boolean isReady) {
        if (user.equals(game.getHost()) && isReady.equals(false) && readyPlayers.get(game.getHost()) != null && readyPlayers.get(game.getHost())) {
            // hosts sets ready to false
            for (final User current : game.getUsersWithDevices()) {
                readyPlayers.put(current, false);
                game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
            }

        } else if (user.equals(game.getHost()) && !checkGameStartable()) {
            // host tries to set ready to true, but not startable - do nothing
            game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
        } else if (unassignedUsers.contains(user)) {
            // do nothing intentionally
        } else {
            // set ready of player
            readyPlayers.put(user, isReady);
            game.getWebSocketService().sendWaitingDataToFrontend(game.getGameCode(), game.getDtoFactory().buildWaitingDataDTO(game));
            checkAllPlayersReadyAndStartGame();
        }
    }

    /**
     * Checks whether all players are ready. If this is the case, game gets started.
     */
    private void checkAllPlayersReadyAndStartGame() {
        if (readyPlayers.get(game.getHost())) {
            for (final User user : readyPlayers.keySet()) {
                if (!readyPlayers.get(user)) {
                    return;
                }
            }
            game.startGame();
        }
    }

    /**
     * Method that checks whether all conditions for a game start are satisfied.
     *
     * @return true iff no unassigneds, enough devices and all teams > 2 users.
     */
    protected boolean checkGameStartable() {
        final boolean unassigned = this.unassignedUsers.size() == 0;
        final boolean devices = game.allTeamsEnoughPlayersWithDevice();
        boolean teamSizes = true;
        for (final Team t : game.getTeams()) {
            if (t.getPlayers().size() < 2) {
                teamSizes = false;
                break;
            }
        }
        return unassigned && devices && teamSizes;
    }

    protected List<User> getUnassignedUsers(){
        return unassignedUsers;
    }

    protected Map<User, Boolean> getReadyPlayers() {
        return readyPlayers;
    }

}
