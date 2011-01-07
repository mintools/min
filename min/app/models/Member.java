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

    @Email
    public String email;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Task> raisedTasks = new ArrayList<Task>();

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL)
    public List<Task> assignedTasks = new ArrayList<Task>();

    public String toString() {
        return username;
    }

    public static Member connect(String username, String password) {
        return Member.find("from Member m where m.username = ? and m.password = ?", username, password).first();
    }

    public static Member connected() {
        String username = Scope.Session.current().get("username");
        if (StringUtils.isNotEmpty(username)) {
            return Member.find("byUsername", username).first();
        }
        return null;
    }
}
