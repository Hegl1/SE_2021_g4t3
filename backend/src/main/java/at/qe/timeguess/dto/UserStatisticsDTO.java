package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

import java.util.Collection;
import java.util.List;

public class UserStatisticsDTO {

    private Collection<GameStatisticsDTO> won_games;
    private Collection<GameStatisticsDTO> lost_games;
    private Category most_played_category;
    private int played_games;
    private List<UserDTO> played_with;

    public UserStatisticsDTO() {
    }

    public UserStatisticsDTO(Collection<GameStatisticsDTO> won_games, Collection<GameStatisticsDTO> lost_games,
                             Category most_played_category, int played_games, List<UserDTO> played_with) {
        this.won_games = won_games;
        this.lost_games = lost_games;
        this.most_played_category = most_played_category;
        this.played_games = played_games;
        this.played_with = played_with;
    }

    public Collection<GameStatisticsDTO> getWon_games() {
        return won_games;
    }

    public Collection<GameStatisticsDTO> getLost_games() {
        return lost_games;
    }

    public Category getMost_played_category() {
        return most_played_category;
    }

    public int getPlayed_games() {
        return played_games;
    }

    public List<UserDTO> getPlayed_with() {
        return played_with;
    }

    public void setWon_games(List<GameStatisticsDTO> won_games) {
        this.won_games = won_games;
    }

    public void setLost_games(List<GameStatisticsDTO> lost_games) {
        this.lost_games = lost_games;
    }

    public void setMost_played_category(Category most_played_category) {
        this.most_played_category = most_played_category;
    }

    public void setPlayed_games(int played_games) {
        this.played_games = played_games;
    }

    public void setPlayed_with(List<UserDTO> played_with) {
        this.played_with = played_with;
    }
}
