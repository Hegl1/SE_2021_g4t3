package at.qe.timeguess.dto;

import java.util.Collection;

public class CategoryExpressionDTO {

    private String categoryName;
    private Collection<String> expressionNames;

    public CategoryExpressionDTO(final String categoryName, final Collection<String> expressionNames) {
        super();
        this.categoryName = categoryName;
        this.expressionNames = expressionNames;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Collection<String> getExpressionNames() {
        return expressionNames;
    }
}
