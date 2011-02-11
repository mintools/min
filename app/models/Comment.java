package models;

import org.hibernate.envers.Audited;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * User: soyoung
 * Date: Jan 10, 2011
 */
@Entity
@Audited
public class Comment extends Model {
    @Required
    public Date createdDate;

    @Required
    @ManyToOne(optional = false)
    public Task task;

    @Required
    @ManyToOne(optional = false)
    public Member member;

    @Required
    @MaxSize(3000)
    @Column(length = 3000)
    public String content;

    public Comment(Task task, Member member, String content) {
        this.createdDate = new Date();
        this.task = task;
        this.member = member;
        this.content = content;
    }

    public String toString() {
        return content;
    }
}
