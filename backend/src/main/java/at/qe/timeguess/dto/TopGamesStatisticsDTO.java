package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

import java.util.List;

public class TopGamesStatisticsDTO implements Comparable<TopGamesStatisticsDTO> {

    private List<TeamStatisticsDTO> teams;
    private Category category;
    private double score_per_time;
    private int duration;

    public TopGamesStatisticsDTO() {
    }

    public TopGamesStatisticsDTO(List<TeamStatisticsDTO> teams, Category category, double score_per_time, int duration) {
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

    public double getScore_per_time() {
        return score_per_time;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public int compareTo(TopGamesStatisticsDTO other) {
        return Double.compare(this.score_per_time, other.score_per_time);
    }
}
