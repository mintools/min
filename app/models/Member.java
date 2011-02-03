package models;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
    @OrderBy("sortOrder DESC")
    public List<Task> raisedTasks;

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL)
    @OrderBy("sortOrder DESC")
    public List<Task> assignedTasks;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    public List<WorkingOn> workingOn;

    public Member() {
        this.raisedTasks = new ArrayList<Task>();
        this.assignedTasks = new ArrayList<Task>();
        this.workingOn = new ArrayList<WorkingOn>();
    }

    public String toString() {
        return username;
    }

    public boolean isWorkingOn(Task task) {
        if (task != null) {
            for (Iterator<WorkingOn> iterator = workingOn.iterator(); iterator.hasNext();) {
                WorkingOn item = iterator.next();
                if (item.task == task) return true;
            }
        }

        return false;
    }

    public static Member connect(String username, String password) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) return null;
        return Member.find("from Member m where m.username = ? and m.password = ?", username, password).first();
    }

    public static List<Member> getMembers() {
        return Member.find("from Member m order by m.username asc").fetch();
    }
}
