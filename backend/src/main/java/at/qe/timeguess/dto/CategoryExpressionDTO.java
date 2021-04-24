package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

import java.util.List;

public class CategoryExpressionDTO {

    private Category category;
    private List<ExpressionDTO> expressions;

    public CategoryExpressionDTO(Category category, List<ExpressionDTO> expressions) {
        super();
        this.category = category;
        this.expressions = expressions;
    }

    public Category getCategory() {
        return category;
    }

    public List<ExpressionDTO> getExpressions() {
        return expressions;
    }
}
