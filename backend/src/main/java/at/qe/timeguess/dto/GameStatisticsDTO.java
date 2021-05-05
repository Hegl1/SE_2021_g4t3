package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

public class GameStatisticsDTO implements Comparable<GameStatisticsDTO> {

    private Category category;
    private int amount;

    public GameStatisticsDTO() {
    }

    public GameStatisticsDTO(Category category, int amount) {
        this.category = category;
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public int compareTo(GameStatisticsDTO other) {
        int compareWins = Integer.compare(other.amount, this.amount);
        if(compareWins == 0) {
            return this.getCategory().getName().compareTo(other.getCategory().getName());
        }
        return compareWins;
    }
}
