package at.qe.timeguess.model.demo;

/**
 * A class which denotes a type of a logEntry.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 */
public enum LogEntryType {

    USER_LOGIN("joined the party"),
    USER_LOGOUT("has left the building");

    private LogEntryType(String descriptionP) {
            this.description = descriptionP;
    }

    private String description;

    public String getDescription() {
            return description;
    }

}
