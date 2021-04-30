package at.qe.timeguess.dto;

public class CategoryInfoDTO {

    private Long id;
    private String name;
    private boolean deletable;
    private int expressions_amount;

    public CategoryInfoDTO() {

    }

    public CategoryInfoDTO(Long id, String name, boolean deletable, int expression_amount) {
        this.id = id;
        this.name = name;
        this.deletable = deletable;
        this.expressions_amount = expression_amount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public int getExpressions_amount() {
        return expressions_amount;
    }
}
