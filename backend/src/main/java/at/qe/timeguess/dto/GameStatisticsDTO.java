package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

public class GameStatisticsDTO {

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
}
