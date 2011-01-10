package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * User: soyoung
 * Date: Jan 10, 2011
 */
@Entity
public class TaskInterest extends Model {
    @ManyToOne(optional = false)
    public Member member;

    public Date dateAssigned;

    @ManyToOne(optional = false)
    public Task task;

    public TaskInterest(Member member, Task task) {
        this.member = member;
        this.task = task;
        this.dateAssigned = new Date();
    }

    public static TaskInterest findTaskInterest(Member member, Task task) {
        return TaskInterest.find("from TaskInterest as t where t.member = ? and t.task = ?", member, task).first();
    }

    public static void removeTaskInterest(Member member, Task task) {
        TaskInterest.delete("from TaskInterest as t where t.member = ? and t.task = ?", member, task);
    }
}
