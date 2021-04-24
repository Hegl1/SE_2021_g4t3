package at.qe.timeguess.dto;

import at.qe.timeguess.model.Category;

import java.util.Collection;
import java.util.List;

public class CategoryExpressionDTO {

    private String category;
    private List<String> expressions;

    public CategoryExpressionDTO(final String category, final List<String> expressions) {
        super();
        this.category = category;
        this.expressions = expressions;
    }

    public String getCategory() {
        return category;
    }

    public Collection<String> getExpressions() {
        return expressions;
    }
}
