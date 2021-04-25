package at.qe.timeguess.dto;

public class ExpressionDTO {

    private Long id;
    private String name;

    public ExpressionDTO() {
    }

    public ExpressionDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
