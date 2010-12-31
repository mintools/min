package models;

import org.apache.commons.lang.StringUtils;
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
    public String username;
    public String password;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Task> tasks = new ArrayList<Task>();

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
