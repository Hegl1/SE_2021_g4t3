package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;

import java.util.List;

public class GlobalStatisticsDTO {

    private int totalGames;
    private int number_correct;
    private int number_incorrect;
    private Category mostPlayedCategory;
    private List<User> mostGamesWon;

    public GlobalStatisticsDTO() {
    }

    public GlobalStatisticsDTO(int totalGames, int number_correct, int number_incorrect, Category mostPlayedCategory, List<User> mostGamesWon) {
        this.totalGames = totalGames;
        this.number_correct = number_correct;
        this.number_incorrect = number_incorrect;
        this.mostPlayedCategory = mostPlayedCategory;
        this.mostGamesWon = mostGamesWon;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public int getNumber_correct() {
        return number_correct;
    }

    public int getNumber_incorrect() {
        return number_incorrect;
    }

    public Category getMostPlayedCategory() {
        return mostPlayedCategory;
    }

    public List<User> getMostGamesWon() {
        return mostGamesWon;
    }
}
