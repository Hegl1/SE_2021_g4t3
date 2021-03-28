package at.qe.timeguess.model.demo;

import java.util.Date;

import at.qe.timeguess.model.User;

/**
 * A class which represents a logEntry.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 */
public class LogEntry {

    private User user;
    private Date timestamp = new Date();
    private LogEntryType logType;

    public LogEntry(User user, LogEntryType logType) {
            super();
            this.user = user;
            this.logType = logType;
    }

    public User getUser() {
            return user;
    }

    public void setUser(User user) {
            this.user = user;
    }

    public Date getTimestamp() {
            return timestamp;
    }

    public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
    }

    public LogEntryType getLogType() {
            return logType;
    }

    public void setLogType(LogEntryType logType) {
            this.logType = logType;
    }

}
