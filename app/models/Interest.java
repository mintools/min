package models;

import org.hibernate.envers.Audited;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * User: soyoung
 * Date: Feb 16, 2011
 */
@Entity
@Audited
public class Interest extends Model {
    @ManyToOne(optional = false)
    public Member member;

    public Date dateAssigned;

    @ManyToOne(optional = false)
    public Task task;

    public Interest(Member member, Task task) {
        this.member = member;
        this.task = task;
        this.dateAssigned = new Date();
    }

    public static Interest findInterest(Member member, Task task) {
        return Interest.find("from Interest as t where t.member = ? and t.task = ?", member, task).first();
    }

    public static void removeInterest(Member member, Task task) {
        Interest.delete("from Interest as t where t.member = ? and t.task = ?", member, task);
    }

    public String toString() {
        return member.username;
    }
}
