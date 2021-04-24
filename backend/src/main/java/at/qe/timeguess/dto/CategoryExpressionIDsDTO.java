package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

import java.util.List;

public class CategoryExpressionIDsDTO {

    private Category category;
    private List<ExpressionDTO> expressions;

    public CategoryExpressionIDsDTO(Category category, List<ExpressionDTO> expressions) {
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
