package models;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.mvc.Scope;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
public class Member extends Model {
    @Required
    public String username;
    @Required
    public String password;

    @Required
    @Email
    public String email;

    public String avatarFilename;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Task> raisedTasks;

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL)
    public List<Task> assignedTasks;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    public List<TaskInterest> taskInterests;

    public Member() {
        this.raisedTasks = new ArrayList<Task>();
        this.assignedTasks = new ArrayList<Task>();
        this.taskInterests = new ArrayList<TaskInterest>();
    }

    public String toString() {
        return username;
    }

    public boolean isInterestedIn(Task task) {
        if (task != null) {
            for (Iterator<TaskInterest> iterator = taskInterests.iterator(); iterator.hasNext();) {
                TaskInterest interest = iterator.next();
                if (interest.task == task) return true;
            }
        }

        return false;
    }

    public Member addInterest(Task task) {
        if (TaskInterest.findTaskInterest(this, task) == null) {
            TaskInterest taskInterest = new TaskInterest(this, task);
            this.taskInterests.add(taskInterest);
            this.save();
        }
        return this;
    }

    public Member removeInterest(Task task) {
        TaskInterest.removeTaskInterest(this, task);

        return this;
    }

    public static Member connect(String username, String password) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) return null;
        return Member.find("from Member m where m.username = ? and m.password = ?", username, password).first();
    }

    public static Member connected() {
        String username = Scope.Session.current().get("username");
        if (StringUtils.isNotEmpty(username)) {
            return Member.find("byUsername", username).first();
        }
        return null;
    }

    public static List<Member> getMembers() {
        return Member.find("from Member m order by m.username asc").fetch();
    }
}
