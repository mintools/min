package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Date;

/**
 * User: soyoung
 * Date: Feb 16, 2011
 */
@Entity
public class EmailSpec extends Model {
    public Long taskId;
    public Date timestamp;
    public String emailType;
    public Boolean sent;

    public EmailSpec(Long taskId) {
        this.taskId = taskId;
        this.timestamp = new Date();
        this.sent = false;
    }
}
