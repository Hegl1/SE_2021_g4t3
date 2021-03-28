package at.qe.timeguess.api.model;

public class Team {
    private long id;
    private String name;
    private Integer points; //weil Wert könnte nicht definiert sein bei Requests deshalb Objekt Wrapper

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
