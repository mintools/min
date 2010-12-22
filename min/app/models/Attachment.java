package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.File;
import java.util.Date;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
public class Attachment extends Model {
    public Date createdDate;
    public String name;
    public String filename;
    public String title;
    public String type;

    @ManyToOne(optional = false)
    public Task task;
}
