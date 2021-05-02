package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

public class CategoryStatisticsDTO {

    private Category category;
    private int number_correct;
    private int number_incorrect;

    public CategoryStatisticsDTO() {
    }

    public CategoryStatisticsDTO(Category category, int number_correct, int number_incorrect) {
        this.category = category;
        this.number_correct = number_correct;
        this.number_incorrect = number_incorrect;
    }

    public Category getCategory() {
        return category;
    }

    public int getNumber_correct() {
        return number_correct;
    }

    public int getNumber_incorrect() {
        return number_incorrect;
    }
}
