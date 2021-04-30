package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

import java.util.List;

public class TopGamesStatisticsDTO {

    private List<TeamStatisticsDTO> teams;
    private Category category;
    private int score_per_time;
    private int duration;

    public TopGamesStatisticsDTO() {
    }

    public TopGamesStatisticsDTO(List<TeamStatisticsDTO> teams, Category category, int score_per_time, int duration) {
        this.teams = teams;
        this.category = category;
        this.score_per_time = score_per_time;
        this.duration = duration;
    }

    public List<TeamStatisticsDTO> getTeams() {
        return teams;
    }

    public Category getCategory() {
        return category;
    }

    public int getScore_per_time() {
        return score_per_time;
    }

    public int getDuration() {
        return duration;
    }
}
